package com.fintech.insights.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class GeneratorAgent {

    private final ChatClient.Builder chatClientBuilder;

    @Value("${spring.ai.openai.chat.options.model:gpt-4o}")
    private String model;

    @Value("${spring.ai.openai.chat.options.temperature:0.7}")
    private double temperature;

    public String generateReport(Long userId, String username, String feedback) {
        ChatClient chatClient = chatClientBuilder
                .defaultAdvisors(new MessageChatMemoryAdvisor(new InMemoryChatMemory()))
                .defaultOptions(org.springframework.ai.chat.prompt.ChatOptions.builder()
                        .model(model)
                        .temperature(temperature)
                        .build())
                .build();

        String systemPrompt = """
            You are a Senior Financial Research Analyst with 15+ years of experience in portfolio management, 
            risk assessment, and investment strategy. Your expertise includes equities, fixed income, derivatives, 
            and alternative investments.
            
            CORE RESPONSIBILITIES:
            1. Analyze user portfolios comprehensively using available tools
            2. Provide data-driven investment recommendations
            3. Assess risk based on user's risk profile and market conditions
            4. Identify rebalancing opportunities and optimization strategies
            5. Deliver clear, actionable insights in professional financial language
            
            ANALYSIS FRAMEWORK:
            - Portfolio Performance: Calculate returns, volatility, and benchmark comparisons
            - Risk Assessment: Evaluate concentration risk, sector exposure, and downside protection
            - Market Analysis: Incorporate current market conditions, trends, and macroeconomic factors
            - Recommendations: Provide specific buy/sell/hold recommendations with rationale
            - Rebalancing: Suggest allocation adjustments to align with risk profile and goals
            
            DATA INTEGRITY REQUIREMENTS:
            - ALWAYS use the fetchPortfolioSummary tool to get user's current holdings
            - ALWAYS use fetchLiveStockQuote for each symbol in the portfolio
            - ALWAYS use fetchMarketNews to get recent news and sentiment
            - ALWAYS use searchFinancialContext to find relevant historical context
            - NEVER fabricate or hallucinate financial data, prices, or figures
            - If data is unavailable, explicitly state this limitation
            - Cross-verify all figures against tool outputs before including in report
            
            REPORT STRUCTURE (Markdown Format):
            # Investment Analysis Report for [Username]
            
            ## Executive Summary
            - Brief overview of portfolio status and key findings
            
            ## Portfolio Performance Analysis
            - Total portfolio value and performance metrics
            - Individual asset performance
            - Sector allocation analysis
            - Comparison with benchmarks (if available)
            
            ## Risk Assessment
            - Current risk profile alignment
            - Concentration risk analysis
            - Volatility and downside risk evaluation
            - Stress test scenarios
            
            ## Market Analysis & Outlook
            - Current market conditions
            - Sector trends and opportunities
            - Macroeconomic factors affecting portfolio
            - Recent news impact analysis
            
            ## Investment Recommendations
            - Specific buy/sell/hold recommendations
            - Rationale for each recommendation
            - Risk-return analysis
            - Time horizon considerations
            
            ## Rebalancing Strategy
            - Current vs. target allocation
            - Specific rebalancing actions
            - Tax implications (if applicable)
            - Implementation timeline
            
            ## Conclusion
            - Summary of key action items
            - Next steps and monitoring points
            
            QUALITY STANDARDS:
            - Use professional financial terminology appropriately
            - Provide specific, actionable recommendations
            - Include quantitative analysis where possible
            - Balance optimism with realistic risk assessment
            - Cite data sources and timeframes
            - Avoid jargon overload while maintaining professional tone
            """;

        String userPrompt;
        if (feedback != null && !feedback.isEmpty()) {
            userPrompt = String.format("""
                REVISE the investment report for user ID: %d (username: %s)
                
                PREVIOUS FEEDBACK TO ADDRESS:
                %s
                
                INSTRUCTIONS:
                1. Address all points raised in the feedback
                2. Correct any factual errors or inconsistencies
                3. Improve areas marked as weak or insufficient
                4. Maintain the same comprehensive structure
                5. Ensure all data is verified against tool outputs
                
                Use the available tools to fetch and verify all data:
                - fetchPortfolioSummary for user's portfolio
                - fetchLiveStockQuote for each symbol
                - fetchMarketNews for recent developments
                - searchFinancialContext for relevant historical analysis
                """, userId, username, feedback);
        } else {
            userPrompt = String.format("""
                Generate a comprehensive investment analysis report for user ID: %d (username: %s)
                
                REQUIRED ANALYSIS STEPS:
                1. Use fetchPortfolioSummary to get the user's complete portfolio details
                2. Extract all stock symbols from the portfolio
                3. Use fetchLiveStockQuote for EACH symbol to get current market data
                4. Use fetchMarketNews for major holdings to get recent news
                5. Use searchFinancialContext to find relevant historical analysis
                6. Analyze the data comprehensively following the framework
                
                CRITICAL REQUIREMENTS:
                - Verify all figures against tool outputs before including
                - Cross-check portfolio values with individual stock prices
                - Ensure recommendations are based on actual data, not assumptions
                - If any data is missing or inconsistent, explicitly state this
                - Provide specific, actionable recommendations with clear rationale
                
                Generate the complete report following the specified structure.
                """, userId, username);
        }

        log.info("Generator Agent generating report for user ID: {}, username: {}, with feedback: {}", 
                userId, username, feedback != null ? "YES" : "NO");

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .content();
    }
}
