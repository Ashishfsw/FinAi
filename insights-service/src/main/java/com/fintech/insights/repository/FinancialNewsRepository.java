package com.fintech.insights.repository;

import com.fintech.insights.entity.FinancialNews;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialNewsRepository extends JpaRepository<FinancialNews, Long> {
    Optional<FinancialNews> findByNewsId(String newsId);
    List<FinancialNews> findByPublishedAtAfter(LocalDateTime dateTime);
    boolean existsByNewsId(String newsId);
}
