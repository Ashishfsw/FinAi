package com.fintech.insights.repository;

import com.fintech.insights.entity.InsightReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface InsightReportRepository extends JpaRepository<InsightReport, Long> {
    List<InsightReport> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<InsightReport> findByUsernameOrderByCreatedAtDesc(String username);
    List<InsightReport> findByStatus(InsightReport.ReportStatus status);
    List<InsightReport> findByCreatedAtAfter(LocalDateTime dateTime);
}
