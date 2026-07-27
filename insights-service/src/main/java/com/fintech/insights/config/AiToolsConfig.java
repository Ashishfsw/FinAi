package com.fintech.insights.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class AiToolsConfig {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Bean
    @Description("Fetches the complete portfolio summary for a user including holdings, risk profile, and asset allocation")
    public Function<String, String> fetchPortfolioSummary() {
        return userId -> {
            try {
                log.info("AI Tool: Fetching portfolio summary for user ID: {}", userId);
                
                String response = webClientBuilder.build()
                        .get()
                        .uri("http://portfolio-service/api/v1/portfolios/{userId}", userId)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                
                log.info("AI Tool: Successfully fetched portfolio summary for user ID: {}", userId);
                return response;
            } catch (Exception e) {
                log.error("AI Tool: Error fetching portfolio summary for user ID {}: {}", userId, e.getMessage());
                return String.format("{\"error\": \"Unable to fetch portfolio summary for user %s: %s\"}", userId, e.getMessage());
            }
        };
    }

    @Bean
    @Description("Fetches live stock quote data including price, change, volume, and market metrics for a given symbol")
    public Function<String, String> fetchLiveStockQuote() {
        return symbol -> {
            try {
                log.info("AI Tool: Fetching live stock quote for symbol: {}", symbol);
                
                String response = webClientBuilder.build()
                        .get()
                        .uri("http://market-data-service/api/v1/market/price/{symbol}", symbol.toUpperCase())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                
                log.info("AI Tool: Successfully fetched stock quote for symbol: {}", symbol);
                return response;
            } catch (Exception e) {
                log.error("AI Tool: Error fetching stock quote for symbol {}: {}", symbol, e.getMessage());
                return String.format("{\"error\": \"Unable to fetch stock quote for %s: %s\"}", symbol, e.getMessage());
            }
        };
    }

    @Bean
    @Description("Fetches recent financial news and market sentiment for a given ticker symbol")
    public Function<String, String> fetchMarketNews() {
        return symbol -> {
            try {
                log.info("AI Tool: Fetching market news for symbol: {}", symbol);
                
                String response = webClientBuilder.build()
                        .get()
                        .uri("http://market-data-service/api/v1/market/news/{symbol}", symbol.toUpperCase())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                
                log.info("AI Tool: Successfully fetched market news for symbol: {}", symbol);
                return response;
            } catch (Exception e) {
                log.error("AI Tool: Error fetching market news for symbol {}: {}", symbol, e.getMessage());
                return String.format("{\"error\": \"Unable to fetch market news for %s: %s\"}", symbol, e.getMessage());
            }
        };
    }

    @Bean
    @Description("Fetches historical price data for a given symbol with specified interval and output size")
    public Function<String, String> fetchHistoricalData() {
        return params -> {
            try {
                String[] parts = params.split(",");
                String symbol = parts[0].trim();
                String interval = parts.length > 1 ? parts[1].trim() : "daily";
                String outputSize = parts.length > 2 ? parts[2].trim() : "compact";
                
                log.info("AI Tool: Fetching historical data for symbol: {}, interval: {}", symbol, interval);
                
                String response = webClientBuilder.build()
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/market-data/historical/{symbol}")
                                .queryParam("interval", interval)
                                .queryParam("outputSize", outputSize)
                                .build(symbol.toUpperCase()))
                        .uri("http://market-data-service/api/v1/market-data/historical/{symbol}")
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                
                log.info("AI Tool: Successfully fetched historical data for symbol: {}", symbol);
                return response;
            } catch (Exception e) {
                log.error("AI Tool: Error fetching historical data for params {}: {}", params, e.getMessage());
                return String.format("{\"error\": \"Unable to fetch historical data: %s\"}", e.getMessage());
            }
        };
    }

    @Bean
    @Description("Performs semantic search on ingested financial news to find relevant context for a query")
    public Function<String, String> searchFinancialContext() {
        return query -> {
            try {
                log.info("AI Tool: Searching financial context for query: {}", query);
                
                String response = webClientBuilder.build()
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/insights/search")
                                .queryParam("query", query)
                                .queryParam("topK", 5)
                                .queryParam("similarityThreshold", 0.7)
                                .build())
                        .uri("http://insights-service/api/v1/insights/search")
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                
                log.info("AI Tool: Successfully found context for query: {}", query);
                return response;
            } catch (Exception e) {
                log.error("AI Tool: Error searching context for query {}: {}", query, e.getMessage());
                return String.format("{\"error\": \"Unable to search context: %s\"}", e.getMessage());
            }
        };
    }
}
