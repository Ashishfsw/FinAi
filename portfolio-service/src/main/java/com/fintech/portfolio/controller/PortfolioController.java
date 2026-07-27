package com.fintech.portfolio.controller;

import com.fintech.portfolio.dto.PortfolioSummary;
import com.fintech.portfolio.entity.AssetHolding;
import com.fintech.portfolio.entity.RebalancingRecommendation;
import com.fintech.portfolio.entity.User;
import com.fintech.portfolio.service.PortfolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    // New API v1 endpoints as requested
    @GetMapping("/portfolios/{userId}")
    public ResponseEntity<PortfolioSummary> getPortfolioSummary(@PathVariable Long userId) {
        PortfolioSummary summary = portfolioService.getPortfolioSummary(userId);
        return ResponseEntity.ok(summary);
    }

    @PostMapping("/portfolios/holdings")
    public ResponseEntity<AssetHolding> addOrUpdateHolding(@Valid @RequestBody AssetHolding assetHolding) {
        AssetHolding saved = portfolioService.addOrUpdateAssetHolding(assetHolding);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Legacy endpoints for backward compatibility
    @PostMapping("/users")
    public ResponseEntity<User> createUser(@Valid @RequestBody User user) {
        User createdUser = portfolioService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = portfolioService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/users/username/{username}")
    public ResponseEntity<User> getUserByUsername(@PathVariable String username) {
        User user = portfolioService.getUserByUsername(username);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = portfolioService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @Valid @RequestBody User userDetails) {
        User updatedUser = portfolioService.updateUser(id, userDetails);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        portfolioService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{userId}/holdings")
    public ResponseEntity<AssetHolding> addAssetHolding(@PathVariable Long userId, @Valid @RequestBody AssetHolding assetHolding) {
        AssetHolding created = portfolioService.addAssetHolding(userId, assetHolding);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/users/{userId}/holdings")
    public ResponseEntity<List<AssetHolding>> getUserAssetHoldings(@PathVariable Long userId) {
        List<AssetHolding> holdings = portfolioService.getUserAssetHoldings(userId);
        return ResponseEntity.ok(holdings);
    }

    @PutMapping("/holdings/{id}")
    public ResponseEntity<AssetHolding> updateAssetHolding(@PathVariable Long id, @Valid @RequestBody AssetHolding assetHolding) {
        AssetHolding updated = portfolioService.updateAssetHolding(id, assetHolding);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/holdings/{id}")
    public ResponseEntity<Void> deleteAssetHolding(@PathVariable Long id) {
        portfolioService.deleteAssetHolding(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/users/{userId}/recommendations")
    public ResponseEntity<RebalancingRecommendation> createRebalancingRecommendation(
            @PathVariable Long userId, 
            @Valid @RequestBody RebalancingRecommendation recommendation) {
        RebalancingRecommendation created = portfolioService.createRebalancingRecommendation(userId, recommendation);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/users/{userId}/recommendations")
    public ResponseEntity<List<RebalancingRecommendation>> getUserRebalancingRecommendations(@PathVariable Long userId) {
        List<RebalancingRecommendation> recommendations = portfolioService.getUserRebalancingRecommendations(userId);
        return ResponseEntity.ok(recommendations);
    }

    @PutMapping("/recommendations/{id}/status")
    public ResponseEntity<RebalancingRecommendation> updateRecommendationStatus(
            @PathVariable Long id,
            @RequestParam RebalancingRecommendation.RecommendationStatus status) {
        RebalancingRecommendation updated = portfolioService.updateRecommendationStatus(id, status);
        return ResponseEntity.ok(updated);
    }
}
