package com.fintech.insights.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.insights.entity.FinancialNews;
import com.fintech.insights.repository.FinancialNewsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RAGService {

    private final VectorStore vectorStore;
    private final FinancialNewsRepository financialNewsRepository;
    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${insights.rag.chunk-size:1000}")
    private int chunkSize;

    @Value("${insights.rag.chunk-overlap:200}")
    private int chunkOverlap;

    @Value("${insights.rag.top-k:5}")
    private int defaultTopK;

    public void ingestFinancialNews(String symbol) {
        try {
            log.info("Fetching financial news for symbol: {}", symbol);

            // Call market-data-service to get news
            String newsResponse = webClientBuilder.build()
                    .get()
                    .uri("http://market-data-service/api/v1/market/news/{symbol}", symbol.toUpperCase())
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Converting news to documents for symbol: {}", symbol);

            List<Document> documents = convertNewsToDocuments(newsResponse, symbol);
            
            if (documents.isEmpty()) {
                log.warn("No documents created from news response for symbol: {}", symbol);
                return;
            }

            // Add documents to vector store
            vectorStore.add(documents);

            // Also save to financial_news table for tracking
            saveFinancialNewsToDatabase(newsResponse, symbol);

            log.info("Successfully ingested {} news articles for symbol: {}", documents.size(), symbol);
        } catch (Exception e) {
            log.error("Error ingesting financial news for {}: {}", symbol, e.getMessage(), e);
            throw new RuntimeException("Failed to ingest financial news for " + symbol, e);
        }
    }

    public List<Document> retrieveRelevantContext(String query) {
        return retrieveRelevantContext(query, defaultTopK);
    }

    public List<Document> retrieveRelevantContext(String query, int topK) {
        SearchRequest searchRequest = SearchRequest.query(query)
                .withTopK(topK)
                .withSimilarityThreshold(0.7);

        log.info("Performing similarity search for query: '{}' with topK: {}", query, topK);
        List<Document> results = vectorStore.similaritySearch(searchRequest);
        log.info("Found {} relevant documents for query: '{}'", results.size(), query);

        return results;
    }

    public List<Document> retrieveRelevantContext(String query, int topK, double similarityThreshold) {
        SearchRequest searchRequest = SearchRequest.query(query)
                .withTopK(topK)
                .withSimilarityThreshold(similarityThreshold);

        log.info("Performing similarity search for query: '{}' with topK: {} and threshold: {}", 
                query, topK, similarityThreshold);
        List<Document> results = vectorStore.similaritySearch(searchRequest);
        log.info("Found {} relevant documents for query: '{}'", results.size(), query);

        return results;
    }

    private List<Document> convertNewsToDocuments(String newsResponse, String symbol) {
        try {
            List<Map<String, Object>> newsList = objectMapper.readValue(
                    newsResponse, 
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            return newsList.stream()
                    .map(newsItem -> {
                        String title = (String) newsItem.get("title");
                        String summary = (String) newsItem.get("summary");
                        String source = (String) newsItem.get("source");
                        String url = (String) newsItem.get("url");
                        String newsId = (String) newsItem.get("id");

                        // Combine title and summary for better semantic search
                        String content = String.format(
                                "%s. %s. Source: %s. Ticker: %s.",
                                title != null ? title : "",
                                summary != null ? summary : "",
                                source != null ? source : "Unknown",
                                symbol
                        );

                        // Create metadata
                        Map<String, Object> metadata = new HashMap<>();
                        metadata.put("symbol", symbol);
                        metadata.put("title", title);
                        metadata.put("source", source);
                        metadata.put("url", url);
                        metadata.put("news_id", newsId);
                        metadata.put("ingested_at", LocalDateTime.now().toString());

                        return new Document(content, metadata);
                    })
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error parsing news response: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private void saveFinancialNewsToDatabase(String newsResponse, String symbol) {
        try {
            List<Map<String, Object>> newsList = objectMapper.readValue(
                    newsResponse,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            for (Map<String, Object> newsItem : newsList) {
                String newsId = (String) newsItem.get("id");
                
                // Check if already exists
                if (financialNewsRepository.existsByNewsId(newsId)) {
                    continue;
                }

                FinancialNews news = FinancialNews.builder()
                        .newsId(newsId)
                        .title((String) newsItem.get("title"))
                        .content((String) newsItem.get("summary"))
                        .source((String) newsItem.get("source"))
                        .url((String) newsItem.get("url"))
                        .build();

                financialNewsRepository.save(news);
            }

            log.info("Saved {} news articles to database for symbol: {}", newsList.size(), symbol);

        } catch (Exception e) {
            log.error("Error saving news to database: {}", e.getMessage());
        }
    }

    public void clearVectorStore() {
        try {
            vectorStore.delete(List.of());
            log.info("Cleared vector store");
        } catch (Exception e) {
            log.error("Error clearing vector store: {}", e.getMessage());
            throw new RuntimeException("Failed to clear vector store", e);
        }
    }

    public Map<String, Object> getVectorStoreStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("chunk_size", chunkSize);
        stats.put("chunk_overlap", chunkOverlap);
        stats.put("default_top_k", defaultTopK);
        stats.put("similarity_threshold", 0.7);
        return stats;
    }
}
