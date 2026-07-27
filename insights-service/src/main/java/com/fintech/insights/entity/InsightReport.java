package com.fintech.insights.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "insight_reports")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsightReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String username;

    @Column(columnDefinition = "TEXT")
    private String reportContent;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String recommendations;

    @Column(columnDefinition = "TEXT")
    private String riskAnalysis;

    @Column(columnDefinition = "TEXT")
    private String marketOutlook;

    private Double confidenceScore;

    private Integer iterationCount;

    @Enumerated(EnumType.STRING)
    private ReportStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum ReportStatus {
        GENERATING,
        EVALUATING,
        COMPLETED,
        FAILED
    }
}
