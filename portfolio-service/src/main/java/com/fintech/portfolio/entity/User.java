package com.fintech.portfolio.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(unique = true)
    private String username;

    @NotBlank
    @Email
    @Column(unique = true)
    private String email;

    @NotBlank
    private String fullName;

    @NotBlank
    @Column(name = "password_hash")
    private String password;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RiskProfile riskProfile = RiskProfile.MODERATE;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private Double totalInvestmentValue = 0.0;

    @Column(precision = 15, scale = 2)
    @Builder.Default
    private Double targetAllocation = 100.0;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<AssetHolding> assetHoldings = new ArrayList<>();

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

    public enum RiskProfile {
        CONSERVATIVE,
        MODERATE,
        AGGRESSIVE
    }
}
