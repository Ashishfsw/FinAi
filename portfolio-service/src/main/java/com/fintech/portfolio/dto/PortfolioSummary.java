package com.fintech.portfolio.dto;

import com.fintech.portfolio.entity.AssetHolding;
import com.fintech.portfolio.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioSummary {

    private Long userId;
    private String username;
    private String email;
    private String fullName;
    private User.RiskProfile riskProfile;
    
    // Portfolio value metrics
    private BigDecimal totalInvestmentValue;
    private BigDecimal totalCurrentValue;
    private BigDecimal totalUnrealizedPnL;
    private BigDecimal totalUnrealizedPnLPercentage;
    
    // Asset allocation
    private Map<String, AssetAllocation> assetAllocations;
    
    // Holdings
    private List<AssetHolding> holdings;
    
    // Performance metrics
    private Integer totalHoldings;
    private BigDecimal bestPerformingAsset;
    private BigDecimal worstPerformingAsset;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AssetAllocation {
        private String symbol;
        private String assetName;
        private AssetHolding.AssetType assetType;
        private BigDecimal currentValue;
        private BigDecimal allocationPercentage;
        private BigDecimal targetAllocation;
        private BigDecimal allocationDifference;
    }
}
