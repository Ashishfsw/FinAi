package com.fintech.insights.service;

import com.fintech.insights.entity.InsightReport;
import com.fintech.insights.repository.InsightReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AgentOrchestrationService {

    private final InsightReportRepository insightReportRepository;
    private final RAGService ragService;
    private final EvaluatorAgent evaluatorAgent;
    private final GeneratorAgent generatorAgent;

    @Value("${insights.agent.max-iterations:3}")
    private int maxIterations;

    @Value("${insights.agent.confidence-threshold:0.8}")
    private double confidenceThreshold;

    @Transactional
    public InsightReport generateInsightReport(Long userId, String username) {
        log.info("Starting insight report generation for user ID: {}, username: {}", userId, username);

        InsightReport report = InsightReport.builder()
                .userId(userId)
                .username(username)
                .status(InsightReport.ReportStatus.GENERATING)
                .iterationCount(0)
                .build();

        report = insightReportRepository.save(report);

        try {
            int iteration = 0;
            String generatedContent = "";
            String feedback = "";
            boolean isValid = false;
            double confidenceScore = 0.0;

            // Evaluator-Optimizer Loop
            while (iteration < maxIterations && !isValid) {
                iteration++;
                log.info("=== Iteration {}/{} for user ID: {} ===", iteration, maxIterations, userId);

                // Update report status
                report.setIterationCount(iteration);
                report.setStatus(InsightReport.ReportStatus.GENERATING);
                insightReportRepository.save(report);

                // Generator Agent: Draft the report
                log.info("Generator Agent: Creating report draft for iteration {}", iteration);
                generatedContent = generatorAgent.generateReport(userId, username, feedback);
                report.setReportContent(generatedContent);
                insightReportRepository.save(report);

                // Evaluator Agent: Evaluate the draft
                log.info("Evaluator Agent: Evaluating report draft for iteration {}", iteration);
                report.setStatus(InsightReport.ReportStatus.EVALUATING);
                insightReportRepository.save(report);

                EvaluationResult evaluation = evaluatorAgent.evaluateReport(generatedContent, userId, username);
                isValid = evaluation.isValid();
                confidenceScore = evaluation.confidence();
                feedback = evaluation.feedback();

                log.info("Evaluation Result - isValid: {}, confidenceScore: {}, feedback length: {}", 
                        isValid, confidenceScore, feedback.length());

                // Update report with evaluation results
                report.setConfidenceScore(confidenceScore);
                insightReportRepository.save(report);

                // Check if we should continue iterating
                if (!isValid && iteration < maxIterations) {
                    log.info("Report not valid. Feedback will be provided for next iteration.");
                    // Feedback is already set and will be passed to next generator call
                } else if (isValid) {
                    log.info("Report validated successfully with confidence: {}", confidenceScore);
                } else {
                    log.warn("Max iterations reached without validation. Using last draft with confidence: {}", confidenceScore);
                }
            }

            // Finalize the report
            report.setStatus(InsightReport.ReportStatus.COMPLETED);
            report.setSummary(extractSummary(generatedContent));
            report.setRecommendations(extractRecommendations(generatedContent));
            report.setRiskAnalysis(extractRiskAnalysis(generatedContent));
            report.setMarketOutlook(extractMarketOutlook(generatedContent));

            log.info("Completed insight report generation for user ID: {} with {} iterations, final confidence: {}", 
                    userId, iteration, confidenceScore);

        } catch (Exception e) {
            log.error("Error generating insight report for user ID {}: {}", userId, e.getMessage(), e);
            report.setStatus(InsightReport.ReportStatus.FAILED);
            report.setReportContent("Report generation failed: " + e.getMessage());
        }

        return insightReportRepository.save(report);
    }

    private String extractSummary(String content) {
        if (content == null || content.isEmpty()) {
            return "Unable to generate summary";
        }
        
        // Simple extraction: get first paragraph or first 200 characters
        String[] paragraphs = content.split("\n\n");
        if (paragraphs.length > 0 && !paragraphs[0].isEmpty()) {
            String firstParagraph = paragraphs[0].replaceAll("#.*", "").trim();
            return firstParagraph.length() > 200 ? firstParagraph.substring(0, 200) + "..." : firstParagraph;
        }
        return content.length() > 200 ? content.substring(0, 200) + "..." : content;
    }

    private String extractRecommendations(String content) {
        if (content == null || content.isEmpty()) {
            return "No recommendations available";
        }
        
        // Extract sections that look like recommendations
        StringBuilder recommendations = new StringBuilder();
        boolean inRecommendationsSection = false;
        
        for (String line : content.split("\n")) {
            String lowerLine = line.toLowerCase();
            if (lowerLine.contains("recommendation") || lowerLine.contains("recommend")) {
                inRecommendationsSection = true;
            }
            if (inRecommendationsSection && (line.startsWith("##") || line.startsWith("#"))) {
                if (!lowerLine.contains("recommendation")) {
                    break;
                }
            }
            if (inRecommendationsSection && !line.trim().isEmpty()) {
                recommendations.append(line).append("\n");
            }
        }
        
        return recommendations.length() > 0 ? recommendations.toString() : "Recommendations section not found in report";
    }

    private String extractRiskAnalysis(String content) {
        if (content == null || content.isEmpty()) {
            return "No risk analysis available";
        }
        
        // Extract sections that look like risk analysis
        StringBuilder riskAnalysis = new StringBuilder();
        boolean inRiskSection = false;
        
        for (String line : content.split("\n")) {
            String lowerLine = line.toLowerCase();
            if (lowerLine.contains("risk")) {
                inRiskSection = true;
            }
            if (inRiskSection && (line.startsWith("##") || line.startsWith("#"))) {
                if (!lowerLine.contains("risk")) {
                    break;
                }
            }
            if (inRiskSection && !line.trim().isEmpty()) {
                riskAnalysis.append(line).append("\n");
            }
        }
        
        return riskAnalysis.length() > 0 ? riskAnalysis.toString() : "Risk analysis section not found in report";
    }

    private String extractMarketOutlook(String content) {
        if (content == null || content.isEmpty()) {
            return "No market outlook available";
        }
        
        // Extract sections that look like market outlook
        StringBuilder marketOutlook = new StringBuilder();
        boolean inOutlookSection = false;
        
        for (String line : content.split("\n")) {
            String lowerLine = line.toLowerCase();
            if (lowerLine.contains("outlook") || lowerLine.contains("market")) {
                inOutlookSection = true;
            }
            if (inOutlookSection && (line.startsWith("##") || line.startsWith("#"))) {
                if (!lowerLine.contains("outlook") && !lowerLine.contains("market")) {
                    break;
                }
            }
            if (inOutlookSection && !line.trim().isEmpty()) {
                marketOutlook.append(line).append("\n");
            }
        }
        
        return marketOutlook.length() > 0 ? marketOutlook.toString() : "Market outlook section not found in report";
    }

    public record EvaluationResult(boolean isValid, double confidence, String feedback) {}

    public InsightReportRepository getInsightReportRepository() {
        return insightReportRepository;
    }
}
