package com.fintech.portfolio.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "asset_holdings")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssetHolding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank
    private String symbol;

    @NotBlank
    private String assetName;

    @Enumerated(EnumType.STRING)
    @NotNull
    private AssetType assetType;

    @NotNull
    @Positive
    @Column(precision = 19, scale = 4)
    private BigDecimal quantity;

    @NotNull
    @Positive
    @Column(precision = 19, scale = 4)
    private BigDecimal averageCostPrice;

    @Column(precision = 19, scale = 4)
    private BigDecimal currentPrice;

    @Column(precision = 15, scale = 2)
    private BigDecimal currentValue;

    @Column(precision = 15, scale = 2)
    private BigDecimal unrealizedPnL;

    @Column(precision = 5, scale = 2)
    private BigDecimal unrealizedPnLPercentage;

    @Column(name = "purchased_at")
    private LocalDateTime purchasedAt;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;

    @PrePersist
    protected void onCreate() {
        purchasedAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }

    public enum AssetType {
        STOCK,
        BOND,
        ETF,
        MUTUAL_FUND,
        CRYPTO,
        COMMODITY,
        CASH
    }
}
