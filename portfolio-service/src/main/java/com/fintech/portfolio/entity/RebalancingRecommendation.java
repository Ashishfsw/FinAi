package com.fintech.portfolio.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "rebalancing_recommendations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RebalancingRecommendation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String symbol;

    private String action;

    @Column(precision = 19, scale = 4)
    private BigDecimal recommendedQuantity;

    @Column(precision = 15, scale = 2)
    private BigDecimal recommendedAmount;

    @Column(precision = 5, scale = 2)
    private BigDecimal currentAllocation;

    @Column(precision = 5, scale = 2)
    private BigDecimal targetAllocation;

    @Column(precision = 5, scale = 2)
    private BigDecimal allocationDifference;

    private String reasoning;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "executed_at")
    private LocalDateTime executedAt;

    @Enumerated(EnumType.STRING)
    private RecommendationStatus status;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = RecommendationStatus.PENDING;
    }

    public enum RecommendationStatus {
        PENDING,
        APPROVED,
        EXECUTED,
        REJECTED
    }
}
