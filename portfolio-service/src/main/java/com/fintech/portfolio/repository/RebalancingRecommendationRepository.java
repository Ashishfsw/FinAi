package com.fintech.portfolio.repository;

import com.fintech.portfolio.entity.RebalancingRecommendation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RebalancingRecommendationRepository extends JpaRepository<RebalancingRecommendation, Long> {
    List<RebalancingRecommendation> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<RebalancingRecommendation> findByUserIdAndStatus(Long userId, RebalancingRecommendation.RecommendationStatus status);
}
