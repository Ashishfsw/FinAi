package com.fintech.insights.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Component
@Slf4j
@RequiredArgsConstructor
public class PortfolioTool {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @QuestionAnswerAdvisor
    public Function<String, String> getUserPortfolio(String username) {
        return symbol -> {
            try {
                return webClientBuilder.build()
                        .get()
                        .uri("http://portfolio-service/portfolio/users/username/{username}", username)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            } catch (Exception e) {
                log.error("Error fetching portfolio for user {}: {}", username, e.getMessage());
                return "{\"error\": \"Unable to fetch portfolio data\"}";
            }
        };
    }

    @QuestionAnswerAdvisor
    public Function<String, String> getUserAssetHoldings(String username) {
        return symbol -> {
            try {
                Long userId = getUserIdFromUsername(username);
                if (userId == null) {
                    return "{\"error\": \"User not found\"}";
                }
                return webClientBuilder.build()
                        .get()
                        .uri("http://portfolio-service/portfolio/users/{userId}/holdings", userId)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
            } catch (Exception e) {
                log.error("Error fetching asset holdings for user {}: {}", username, e.getMessage());
                return "{\"error\": \"Unable to fetch asset holdings\"}";
            }
        };
    }

    private Long getUserIdFromUsername(String username) {
        try {
            String response = webClientBuilder.build()
                    .get()
                    .uri("http://portfolio-service/portfolio/users/username/{username}", username)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            JsonNode jsonNode = objectMapper.readTree(response);
            return jsonNode.has("id") ? jsonNode.get("id").asLong() : null;
        } catch (Exception e) {
            log.error("Error getting user ID from username {}: {}", username, e.getMessage());
            return null;
        }
    }
}
