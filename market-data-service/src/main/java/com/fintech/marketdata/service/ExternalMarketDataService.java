package com.fintech.marketdata.service;

import com.fintech.marketdata.dto.HistoricalData;
import com.fintech.marketdata.dto.MarketNews;
import com.fintech.marketdata.dto.StockQuote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class ExternalMarketDataService {

    private final WebClient.Builder webClientBuilder;
    private final MockMarketDataService mockMarketDataService;

    @Value("${market-data.external-api.alpha-vantage.base-url}")
    private String alphaVantageBaseUrl;

    @Value("${market-data.external-api.alpha-vantage.api-key:demo}")
    private String apiKey;

    @Value("${market-data.use-mock-fallback:true}")
    private boolean useMockFallback;

    @Cacheable(value = "stockQuotes", key = "#symbol", unless = "#result == null")
    public Mono<StockQuote> getStockQuote(String symbol) {
        String url = String.format("%s?function=GLOBAL_QUOTE&symbol=%s&apikey=%s",
                alphaVantageBaseUrl, symbol, apiKey);

        log.info("Fetching stock quote for symbol: {}", symbol);

        return webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::parseStockQuote)
                .doOnError(error -> log.error("Error fetching stock quote for {}: {}", symbol, error.getMessage()))
                .onErrorResume(error -> {
                    if (useMockFallback || isRateLimitError(error)) {
                        log.warn("Using mock fallback for stock quote: {}", symbol);
                        return Mono.just(mockMarketDataService.getMockStockQuote(symbol));
                    }
                    return Mono.error(error);
                });
    }

    @Cacheable(value = "historicalData", key = "#symbol + '-' + #interval + '-' + #outputSize")
    public Mono<HistoricalData> getHistoricalData(String symbol, String interval, String outputSize) {
        String function = determineTimeSeriesFunction(interval);
        String url = String.format("%s?function=%s&symbol=%s&interval=%s&outputsize=%s&apikey=%s",
                alphaVantageBaseUrl, function, symbol, interval, outputSize, apiKey);

        log.info("Fetching historical data for symbol: {}, interval: {}", symbol, interval);

        return webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> parseHistoricalData(response, symbol, interval, outputSize))
                .doOnError(error -> log.error("Error fetching historical data for {}: {}", symbol, error.getMessage()))
                .onErrorResume(error -> {
                    if (useMockFallback || isRateLimitError(error)) {
                        log.warn("Using mock fallback for historical data: {}", symbol);
                        return Mono.just(mockMarketDataService.getMockHistoricalData(symbol, interval, outputSize));
                    }
                    return Mono.error(error);
                });
    }

    @Cacheable(value = "marketNews", key = "#symbol")
    public Mono<List<MarketNews>> getMarketNews(String symbol) {
        String url = String.format("%s?function=NEWS_SENTIMENT&tickers=%s&apikey=%s",
                alphaVantageBaseUrl, symbol, apiKey);

        log.info("Fetching market news for symbol: {}", symbol);

        return webClientBuilder.build()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(Map.class)
                .map(this::parseMarketNews)
                .doOnError(error -> log.error("Error fetching market news for {}: {}", symbol, error.getMessage()))
                .onErrorResume(error -> {
                    if (useMockFallback || isRateLimitError(error)) {
                        log.warn("Using mock fallback for market news: {}", symbol);
                        return Mono.just(mockMarketDataService.getMockMarketNews(symbol));
                    }
                    return Mono.error(error);
                });
    }

    private StockQuote parseStockQuote(Map<String, Object> response) {
        Map<String, String> quoteData = (Map<String, String>) response.get("Global Quote");
        if (quoteData == null) {
            return null;
        }

        return StockQuote.builder()
                .symbol(quoteData.get("01. symbol"))
                .price(parseBigDecimal(quoteData.get("05. price")))
                .change(parseBigDecimal(quoteData.get("09. change")))
                .changePercent(parsePercentage(quoteData.get("10. change percent")))
                .open(parseBigDecimal(quoteData.get("02. open")))
                .high(parseBigDecimal(quoteData.get("03. high")))
                .low(parseBigDecimal(quoteData.get("04. low")))
                .previousClose(parseBigDecimal(quoteData.get("08. previous close")))
                .volume(parseLong(quoteData.get("06. volume")))
                .timestamp(parseDateTime(quoteData.get("07. latest trading day")))
                .build();
    }

    private HistoricalData parseHistoricalData(Map<String, Object> response, String symbol, String interval, String outputSize) {
        String timeSeriesKey = determineTimeSeriesKey(interval);
        Map<String, Map<String, String>> timeSeriesData = (Map<String, Map<String, String>>) response.get(timeSeriesKey);
        
        if (timeSeriesData == null) {
            return HistoricalData.builder()
                    .symbol(symbol)
                    .interval(interval)
                    .outputSize(outputSize)
                    .pricePoints(new ArrayList<>())
                    .build();
        }

        List<HistoricalData.PricePoint> pricePoints = timeSeriesData.entrySet().stream()
                .map(entry -> HistoricalData.PricePoint.builder()
                        .date(parseLocalDate(entry.getKey()))
                        .open(parseBigDecimal(entry.getValue().get("1. open")))
                        .high(parseBigDecimal(entry.getValue().get("2. high")))
                        .low(parseBigDecimal(entry.getValue().get("3. low")))
                        .close(parseBigDecimal(entry.getValue().get("4. close")))
                        .volume(parseLong(entry.getValue().get("5. volume")))
                        .build())
                .toList();

        return HistoricalData.builder()
                .symbol(symbol)
                .pricePoints(pricePoints)
                .interval(interval)
                .outputSize(outputSize)
                .build();
    }

    @SuppressWarnings("unchecked")
    private List<MarketNews> parseMarketNews(Map<String, Object> response) {
        List<Map<String, Object>> newsData = (List<Map<String, Object>>) response.get("feed");
        if (newsData == null) {
            return new ArrayList<>();
        }

        return newsData.stream()
                .map(news -> MarketNews.builder()
                        .id((String) news.get("id"))
                        .title((String) news.get("title"))
                        .summary((String) news.get("summary"))
                        .url((String) news.get("url"))
                        .source((String) news.get("source"))
                        .publishedAt(parseDateTime((String) news.get("time_published")))
                        .symbols((List<String>) news.get("ticker_sentiment"))
                        .sentiment((String) news.get("overall_sentiment_score"))
                        .relevanceScore(parseDouble(news.get("relevance_score")))
                        .build())
                .toList();
    }

    private String determineTimeSeriesFunction(String interval) {
        return switch (interval.toLowerCase()) {
            case "1min", "5min", "15min", "30min", "60min" -> "TIME_SERIES_INTRADAY";
            case "daily" -> "TIME_SERIES_DAILY";
            case "weekly" -> "TIME_SERIES_WEEKLY";
            case "monthly" -> "TIME_SERIES_MONTHLY";
            default -> "TIME_SERIES_DAILY";
        };
    }

    private String determineTimeSeriesKey(String interval) {
        return switch (interval.toLowerCase()) {
            case "1min" -> "Time Series (1min)";
            case "5min" -> "Time Series (5min)";
            case "15min" -> "Time Series (15min)";
            case "30min" -> "Time Series (30min)";
            case "60min" -> "Time Series (60min)";
            case "daily" -> "Time Series (Daily)";
            case "weekly" -> "Weekly Time Series";
            case "monthly" -> "Monthly Time Series";
            default -> "Time Series (Daily)";
        };
    }

    private BigDecimal parseBigDecimal(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private BigDecimal parsePercentage(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return new BigDecimal(value.replace("%", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            if (value.contains("-")) {
                return LocalDate.parse(value).atStartOfDay();
            }
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
            return LocalDateTime.parse(value, formatter);
        } catch (Exception e) {
            return null;
        }
    }

    private LocalDate parseLocalDate(String value) {
        if (value == null || value.isEmpty()) return null;
        try {
            return LocalDate.parse(value);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isRateLimitError(Throwable error) {
        String errorMessage = error.getMessage() != null ? error.getMessage().toLowerCase() : "";
        return errorMessage.contains("rate limit") || 
               errorMessage.contains("429") || 
               errorMessage.contains("too many requests") ||
               errorMessage.contains("api call frequency");
    }
}
