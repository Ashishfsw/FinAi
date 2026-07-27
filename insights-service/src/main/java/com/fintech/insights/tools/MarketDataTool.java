package com.fintech.insights.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.function.Function;

@Component
@Slf4j
@RequiredArgsConstructor
public class MarketDataTool {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @QuestionAnswerAdvisor
    public Function<String, String> getStockQuote(String symbol) {
        return input -> {
            try {
                return webClientBuilder.build()
                        .get()
                        .uri("http://market-data-service/market-data/quote/{symbol}", symbol.toUpperCase())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            } catch (Exception e) {
                log.error("Error fetching stock quote for {}: {}", symbol, e.getMessage());
                return "{\"error\": \"Unable to fetch stock quote\"}";
            }
        };
    }

    @QuestionAnswerAdvisor
    public Function<String, String> getMarketNews(String symbol) {
        return input -> {
            try {
                return webClientBuilder.build()
                        .get()
                        .uri("http://market-data-service/market-data/news/{symbol}", symbol.toUpperCase())
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            } catch (Exception e) {
                log.error("Error fetching market news for {}: {}", symbol, e.getMessage());
                return "{\"error\": \"Unable to fetch market news\"}";
            }
        };
    }

    @QuestionAnswerAdvisor
    public Function<String, String> getHistoricalData(String params) {
        return input -> {
            try {
                String[] parts = params.split(",");
                String symbol = parts[0].trim();
                String interval = parts.length > 1 ? parts[1].trim() : "daily";
                String outputSize = parts.length > 2 ? parts[2].trim() : "compact";
                
                return webClientBuilder.build()
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/market-data/historical/{symbol}")
                                .queryParam("interval", interval)
                                .queryParam("outputSize", outputSize)
                                .build(symbol.toUpperCase()))
                        .uri("http://market-data-service/market-data/historical/{symbol}")
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            } catch (Exception e) {
                log.error("Error fetching historical data for {}: {}", params, e.getMessage());
                return "{\"error\": \"Unable to fetch historical data\"}";
            }
        };
    }
}
