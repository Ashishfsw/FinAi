package com.fintech.insights.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class EvaluatorAgent {

    private final ChatClient.Builder chatClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${spring.ai.openai.chat.options.model:gpt-4o}")
    private String model;

    @Value("${insights.agent.confidence-threshold:0.8}")
    private double confidenceThreshold;

    public AgentOrchestrationService.EvaluationResult evaluateReport(String reportContent, Long userId, String username) {
        ChatClient chatClient = chatClientBuilder
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                .defaultOptions(org.springframework.ai.chat.prompt.ChatOptions.builder()
                        .model(model)
                        .temperature(0.3)  // Lower temperature for more consistent evaluation
                        .build())
                .build();

        String systemPrompt = """
            You are a Critical Compliance & Audit Inspector with expertise in financial reporting standards, 
            regulatory compliance, and quality assurance. Your role is to rigorously evaluate investment reports 
            for accuracy, completeness, and compliance with professional standards.
            
            EVALUATION CRITERIA:
            
            1. FACTUAL ACCURACY (Weight: 30%)
               - Verify all numerical figures against provided tool outputs
               - Cross-check portfolio values with individual stock prices
               - Ensure percentages and calculations are mathematically correct
               - Validate that all cited data matches actual tool responses
            
            2. DATA INTEGRITY (Weight: 25%)
               - Ensure no hallucinated or fabricated data
               - Verify all stock symbols mentioned exist in the portfolio
               - Confirm that market data is current and accurate
               - Check that risk assessments align with actual portfolio composition
            
            3. CONSISTENCY (Weight: 20%)
               - Ensure no contradictions within the report
               - Verify that recommendations align with risk profile
               - Check that analysis is consistent with provided data
               - Validate that timeframes and dates are consistent
            
            4. COMPLETENESS (Weight: 15%)
               - Ensure all required sections are present
               - Verify that all portfolio holdings are analyzed
               - Check that risk assessment is comprehensive
               - Confirm that recommendations are specific and actionable
            
            5. PROFESSIONAL STANDARDS (Weight: 10%)
               - Evaluate clarity and readability
               - Assess appropriateness of financial terminology
               - Check that recommendations are actionable
               - Verify that disclaimers and limitations are included
            
            EVALUATION PROCESS:
            1. Use fetchPortfolioSummary to verify portfolio data
            2. Use fetchLiveStockQuote to verify price data for mentioned symbols
            3. Cross-reference all figures in the report with tool outputs
            4. Identify any discrepancies, inconsistencies, or missing information
            5. Assess overall quality and compliance with standards
            
            OUTPUT REQUIREMENTS:
            You MUST output a strict JSON object with the following structure:
            {
                "isValid": boolean,
                "confidenceScore": double (0.0 to 1.0),
                "feedback": string (detailed feedback),
                "issues": array of strings (specific issues found, if any),
                "strengths": array of strings (notable strengths, if any)
            }
            
            - isValid: true only if all critical criteria are met and confidenceScore >= threshold
            - confidenceScore: overall quality score from 0.0 (poor) to 1.0 (excellent)
            - feedback: comprehensive summary of evaluation findings
            - issues: specific problems identified (empty array if none)
            - strengths: notable positive aspects (empty array if none)
            
            CRITICAL STANDARDS:
            - Any hallucinated data = isValid = false
            - Mathematical errors = isValid = false
            - Missing required sections = isValid = false
            - Contradictory statements = isValid = false
            - Recommendations not based on data = isValid = false
            """;

        String userPrompt = String.format("""
            EVALUATE the following investment report for user ID: %d (username: %s)
            
            REPORT CONTENT:
            %s
            
            EVALUATION INSTRUCTIONS:
            1. Use fetchPortfolioSummary to verify the user's actual portfolio data
            2. Use fetchLiveStockQuote for each symbol mentioned to verify price data
            3. Cross-check all numerical figures in the report against tool outputs
            4. Identify any discrepancies, hallucinations, or inconsistencies
            5. Assess completeness and professional quality
            6. Provide specific, actionable feedback
            
            OUTPUT: Return ONLY the strict JSON object as specified in the system prompt.
            Do not include any additional text or explanations outside the JSON structure.
            """, userId, username, reportContent);

        log.info("Evaluator Agent evaluating report for user ID: {}, username: {}", userId, username);

        try {
            String response = chatClient.prompt()
                    .system(systemPrompt)
                    .user(userPrompt)
                    .call()
                    .content();

            log.info("Evaluator Agent response received for user ID: {}", userId);
            return parseEvaluationResponse(response);

        } catch (Exception e) {
            log.error("Error during evaluation for user ID {}: {}", userId, e.getMessage(), e);
            return new AgentOrchestrationService.EvaluationResult(
                    false, 
                    0.0, 
                    "Evaluation failed due to system error: " + e.getMessage()
            );
        }
    }

    private AgentOrchestrationService.EvaluationResult parseEvaluationResponse(String response) {
        try {
            // Clean up the response to extract JSON
            String jsonStr = response;
            
            // Remove markdown code blocks if present
            if (response.contains("```json")) {
                jsonStr = response.substring(response.indexOf("```json") + 7);
                jsonStr = jsonStr.substring(0, jsonStr.indexOf("```"));
            } else if (response.contains("```")) {
                jsonStr = response.substring(response.indexOf("```") + 3);
                jsonStr = jsonStr.substring(0, jsonStr.indexOf("```"));
            }
            
            jsonStr = jsonStr.trim();

            JsonNode rootNode = objectMapper.readTree(jsonStr);

            boolean isValid = rootNode.path("isValid").asBoolean(false);
            double confidenceScore = rootNode.path("confidenceScore").asDouble(0.0);
            String feedback = rootNode.path("feedback").asText("No feedback provided");

            log.info("Parsed evaluation - isValid: {}, confidenceScore: {}", isValid, confidenceScore);

            return new AgentOrchestrationService.EvaluationResult(
                    isValid,
                    confidenceScore,
                    feedback
            );

        } catch (Exception e) {
            log.error("Error parsing evaluation response: {}", e.getMessage());
            log.error("Response was: {}", response);
            
            // Fallback parsing if JSON parsing fails
            return new AgentOrchestrationService.EvaluationResult(
                    false,
                    0.0,
                    "Failed to parse evaluation response. Original response: " + response
            );
        }
    }
}
