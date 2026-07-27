package com.fintech.insights.controller;

import com.fintech.insights.entity.InsightReport;
import com.fintech.insights.service.AgentOrchestrationService;
import com.fintech.insights.service.RAGService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class InsightsController {

    private final AgentOrchestrationService agentOrchestrationService;
    private final RAGService ragService;

    // New API v1 Multi-Agent Orchestration endpoint as requested
    @PostMapping("/insights/generate-report/{userId}")
    public ResponseEntity<InsightReport> generateReport(
            @PathVariable Long userId,
            @RequestParam(required = false) String username) {
        try {
            // If username not provided, use a default or fetch from user service
            String effectiveUsername = username != null ? username : "user_" + userId;
            InsightReport report = agentOrchestrationService.generateInsightReport(userId, effectiveUsername);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate report: " + e.getMessage(), e);
        }
    }

    // New API v1 RAG endpoints as requested
    @PostMapping("/insights/ingest-news/{ticker}")
    public ResponseEntity<String> ingestNews(@PathVariable String ticker) {
        try {
            ragService.ingestFinancialNews(ticker);
            return ResponseEntity.ok("News ingestion completed for ticker: " + ticker);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to ingest news: " + e.getMessage());
        }
    }

    @GetMapping("/insights/search")
    public ResponseEntity<List<Document>> searchDocuments(
            @RequestParam String query,
            @RequestParam(defaultValue = "5") int topK,
            @RequestParam(defaultValue = "0.7") double similarityThreshold) {
        List<Document> results = ragService.retrieveRelevantContext(query, topK, similarityThreshold);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/insights/vector-store/stats")
    public ResponseEntity<Map<String, Object>> getVectorStoreStats() {
        return ResponseEntity.ok(ragService.getVectorStoreStats());
    }

    // Legacy endpoints for backward compatibility
    @PostMapping("/insights/reports/generate")
    public ResponseEntity<InsightReport> generateReportLegacy(
            @RequestParam Long userId,
            @RequestParam String username) {
        InsightReport report = agentOrchestrationService.generateInsightReport(userId, username);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/insights/reports/user/{userId}")
    public ResponseEntity<List<InsightReport>> getUserReports(@PathVariable Long userId) {
        List<InsightReport> reports = agentOrchestrationService.getInsightReportRepository()
                .findByUserIdOrderByCreatedAtDesc(userId);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/insights/reports/username/{username}")
    public ResponseEntity<List<InsightReport>> getReportsByUsername(@PathVariable String username) {
        List<InsightReport> reports = agentOrchestrationService.getInsightReportRepository()
                .findByUsernameOrderByCreatedAtDesc(username);
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/insights/reports/{id}")
    public ResponseEntity<InsightReport> getReportById(@PathVariable Long id) {
        InsightReport report = agentOrchestrationService.getInsightReportRepository()
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Report not found"));
        return ResponseEntity.ok(report);
    }

    @PostMapping("/rag/ingest/{symbol}")
    public ResponseEntity<String> ingestNewsLegacy(@PathVariable String symbol) {
        try {
            ragService.ingestFinancialNews(symbol);
            return ResponseEntity.ok("News ingestion started for symbol: " + symbol);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to ingest news: " + e.getMessage());
        }
    }

    @DeleteMapping("/rag/clear")
    public ResponseEntity<String> clearVectorStore() {
        try {
            ragService.clearVectorStore();
            return ResponseEntity.ok("Vector store cleared");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to clear vector store: " + e.getMessage());
        }
    }
}
