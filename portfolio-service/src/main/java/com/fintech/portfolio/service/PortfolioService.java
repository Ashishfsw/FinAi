package com.fintech.portfolio.service;

import com.fintech.portfolio.dto.PortfolioSummary;
import com.fintech.portfolio.entity.AssetHolding;
import com.fintech.portfolio.entity.RebalancingRecommendation;
import com.fintech.portfolio.entity.User;
import com.fintech.portfolio.repository.AssetHoldingRepository;
import com.fintech.portfolio.repository.RebalancingRecommendationRepository;
import com.fintech.portfolio.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PortfolioService {

    private final UserRepository userRepository;
    private final AssetHoldingRepository assetHoldingRepository;
    private final RebalancingRecommendationRepository rebalancingRecommendationRepository;
    private final PasswordEncoder passwordEncoder;

    public User createUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode("change-me-" + System.currentTimeMillis()));
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found with username: " + username));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(Long id, User userDetails) {
        User user = getUserById(id);
        user.setFullName(userDetails.getFullName());
        user.setEmail(userDetails.getEmail());
        user.setRiskProfile(userDetails.getRiskProfile());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = getUserById(id);
        assetHoldingRepository.deleteByUserId(id);
        userRepository.delete(user);
    }

    public AssetHolding addAssetHolding(Long userId, AssetHolding assetHolding) {
        User user = getUserById(userId);
        assetHolding.setUser(user);
        
        BigDecimal currentValue = assetHolding.getQuantity()
                .multiply(assetHolding.getCurrentPrice() != null ? 
                        assetHolding.getCurrentPrice() : assetHolding.getAverageCostPrice())
                .setScale(2, RoundingMode.HALF_UP);
        assetHolding.setCurrentValue(currentValue);
        
        AssetHolding saved = assetHoldingRepository.save(assetHolding);
        updateUserTotalInvestment(userId);
        return saved;
    }

    public AssetHolding updateAssetHolding(Long id, AssetHolding assetHoldingDetails) {
        AssetHolding assetHolding = assetHoldingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Asset holding not found with id: " + id));
        
        assetHolding.setQuantity(assetHoldingDetails.getQuantity());
        assetHolding.setCurrentPrice(assetHoldingDetails.getCurrentPrice());
        
        if (assetHolding.getCurrentPrice() != null) {
            BigDecimal currentValue = assetHolding.getQuantity()
                    .multiply(assetHolding.getCurrentPrice())
                    .setScale(2, RoundingMode.HALF_UP);
            assetHolding.setCurrentValue(currentValue);
            
            BigDecimal costBasis = assetHolding.getQuantity()
                    .multiply(assetHolding.getAverageCostPrice())
                    .setScale(2, RoundingMode.HALF_UP);
            
            BigDecimal unrealizedPnL = currentValue.subtract(costBasis);
            assetHolding.setUnrealizedPnL(unrealizedPnL);
            
            if (costBasis.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal pnlPercentage = unrealizedPnL
                        .divide(costBasis, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100"))
                        .setScale(2, RoundingMode.HALF_UP);
                assetHolding.setUnrealizedPnLPercentage(pnlPercentage);
            }
        }
        
        AssetHolding saved = assetHoldingRepository.save(assetHolding);
        updateUserTotalInvestment(assetHolding.getUser().getId());
        return saved;
    }

    public List<AssetHolding> getUserAssetHoldings(Long userId) {
        return assetHoldingRepository.findByUserId(userId);
    }

    public void deleteAssetHolding(Long id) {
        AssetHolding assetHolding = assetHoldingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Asset holding not found with id: " + id));
        Long userId = assetHolding.getUser().getId();
        assetHoldingRepository.delete(assetHolding);
        updateUserTotalInvestment(userId);
    }

    public RebalancingRecommendation createRebalancingRecommendation(Long userId, RebalancingRecommendation recommendation) {
        User user = getUserById(userId);
        recommendation.setUser(user);
        return rebalancingRecommendationRepository.save(recommendation);
    }

    public List<RebalancingRecommendation> getUserRebalancingRecommendations(Long userId) {
        return rebalancingRecommendationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public RebalancingRecommendation updateRecommendationStatus(Long id, RebalancingRecommendation.RecommendationStatus status) {
        RebalancingRecommendation recommendation = rebalancingRecommendationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Recommendation not found with id: " + id));
        recommendation.setStatus(status);
        if (status == RebalancingRecommendation.RecommendationStatus.EXECUTED) {
            recommendation.setExecutedAt(java.time.LocalDateTime.now());
        }
        return rebalancingRecommendationRepository.save(recommendation);
    }

    private void updateUserTotalInvestment(Long userId) {
        List<AssetHolding> holdings = assetHoldingRepository.findByUserId(userId);
        BigDecimal total = holdings.stream()
                .map(h -> h.getCurrentValue() != null ? h.getCurrentValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        User user = getUserById(userId);
        user.setTotalInvestmentValue(total.doubleValue());
        userRepository.save(user);
    }

    // New API v1 methods
    public PortfolioSummary getPortfolioSummary(Long userId) {
        User user = getUserById(userId);
        List<AssetHolding> holdings = getUserAssetHoldings(userId);
        
        BigDecimal totalCurrentValue = holdings.stream()
                .map(h -> h.getCurrentValue() != null ? h.getCurrentValue() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalCostBasis = holdings.stream()
                .map(h -> h.getQuantity().multiply(h.getAverageCostPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal totalUnrealizedPnL = totalCurrentValue.subtract(totalCostBasis);
        BigDecimal totalUnrealizedPnLPercentage = totalCostBasis.compareTo(BigDecimal.ZERO) > 0 
                ? totalUnrealizedPnL.divide(totalCostBasis, 4, RoundingMode.HALF_UP)
                        .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        Map<String, PortfolioSummary.AssetAllocation> allocations = calculateAssetAllocations(holdings, totalCurrentValue);
        
        BigDecimal bestPerformingAsset = holdings.stream()
                .filter(h -> h.getUnrealizedPnLPercentage() != null)
                .map(AssetHolding::getUnrealizedPnLPercentage)
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        BigDecimal worstPerformingAsset = holdings.stream()
                .filter(h -> h.getUnrealizedPnLPercentage() != null)
                .map(AssetHolding::getUnrealizedPnLPercentage)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        
        return PortfolioSummary.builder()
                .userId(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .riskProfile(user.getRiskProfile())
                .totalInvestmentValue(BigDecimal.valueOf(user.getTotalInvestmentValue()))
                .totalCurrentValue(totalCurrentValue)
                .totalUnrealizedPnL(totalUnrealizedPnL)
                .totalUnrealizedPnLPercentage(totalUnrealizedPnLPercentage)
                .assetAllocations(allocations)
                .holdings(holdings)
                .totalHoldings(holdings.size())
                .bestPerformingAsset(bestPerformingAsset)
                .worstPerformingAsset(worstPerformingAsset)
                .build();
    }

    public AssetHolding addOrUpdateAssetHolding(AssetHolding assetHolding) {
        if (assetHolding.getUser() == null || assetHolding.getUser().getId() == null) {
            throw new IllegalArgumentException("User ID is required");
        }
        
        Long userId = assetHolding.getUser().getId();
        
        // Check if holding already exists for this user and symbol
        Optional<AssetHolding> existing = assetHoldingRepository.findByUserIdAndSymbol(userId, assetHolding.getSymbol());
        
        if (existing.isPresent()) {
            // Update existing holding
            AssetHolding existingHolding = existing.get();
            existingHolding.setQuantity(assetHolding.getQuantity());
            existingHolding.setCurrentPrice(assetHolding.getCurrentPrice());
            existingHolding.setAverageCostPrice(assetHolding.getAverageCostPrice());
            return updateAssetHolding(existingHolding.getId(), existingHolding);
        } else {
            // Add new holding
            return addAssetHolding(userId, assetHolding);
        }
    }

    private Map<String, PortfolioSummary.AssetAllocation> calculateAssetAllocations(
            List<AssetHolding> holdings, BigDecimal totalCurrentValue) {
        
        if (totalCurrentValue.compareTo(BigDecimal.ZERO) == 0) {
            return new HashMap<>();
        }
        
        return holdings.stream()
                .collect(Collectors.toMap(
                        AssetHolding::getSymbol,
                        holding -> {
                            BigDecimal allocationPercentage = holding.getCurrentValue()
                                    .divide(totalCurrentValue, 4, RoundingMode.HALF_UP)
                                    .multiply(new BigDecimal("100"))
                                    .setScale(2, RoundingMode.HALF_UP);
                            
                            return PortfolioSummary.AssetAllocation.builder()
                                    .symbol(holding.getSymbol())
                                    .assetName(holding.getAssetName())
                                    .assetType(holding.getAssetType())
                                    .currentValue(holding.getCurrentValue())
                                    .allocationPercentage(allocationPercentage)
                                    .targetAllocation(BigDecimal.valueOf(100.0 / holdings.size()))
                                    .allocationDifference(allocationPercentage.subtract(
                                            BigDecimal.valueOf(100.0 / holdings.size())))
                                    .build();
                        }
                ));
    }
}
