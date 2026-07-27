package com.fintech.marketdata.service;

import com.fintech.marketdata.dto.HistoricalData;
import com.fintech.marketdata.dto.MarketNews;
import com.fintech.marketdata.dto.StockQuote;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
@Slf4j
public class MockMarketDataService {

    private final Random random = new Random();

    public StockQuote getMockStockQuote(String symbol) {
        log.info("Returning mock stock quote for symbol: {}", symbol);
        
        double basePrice = getBasePriceForSymbol(symbol);
        double currentPrice = basePrice + (random.nextDouble() * 10 - 5);
        double openPrice = currentPrice + (random.nextDouble() * 2 - 1);
        double highPrice = currentPrice + random.nextDouble() * 3;
        double lowPrice = currentPrice - random.nextDouble() * 3;
        double previousClose = currentPrice + (random.nextDouble() * 4 - 2);
        
        double change = currentPrice - previousClose;
        double changePercent = (change / previousClose) * 100;
        
        return StockQuote.builder()
                .symbol(symbol)
                .name(getCompanyNameForSymbol(symbol))
                .price(BigDecimal.valueOf(currentPrice).setScale(2, BigDecimal.ROUND_HALF_UP))
                .change(BigDecimal.valueOf(change).setScale(2, BigDecimal.ROUND_HALF_UP))
                .changePercent(BigDecimal.valueOf(changePercent).setScale(2, BigDecimal.ROUND_HALF_UP))
                .open(BigDecimal.valueOf(openPrice).setScale(2, BigDecimal.ROUND_HALF_UP))
                .high(BigDecimal.valueOf(highPrice).setScale(2, BigDecimal.ROUND_HALF_UP))
                .low(BigDecimal.valueOf(lowPrice).setScale(2, BigDecimal.ROUND_HALF_UP))
                .previousClose(BigDecimal.valueOf(previousClose).setScale(2, BigDecimal.ROUND_HALF_UP))
                .volume((long) (random.nextInt(10000000) + 1000000))
                .timestamp(LocalDateTime.now())
                .exchange("NASDAQ")
                .build();
    }

    public List<MarketNews> getMockMarketNews(String symbol) {
        log.info("Returning mock market news for symbol: {}", symbol);
        
        List<MarketNews> news = new ArrayList<>();
        String[] headlines = {
            symbol + " Reports Strong Quarterly Earnings",
            "Analysts Upgrade " + symbol + " to Buy Rating",
            symbol + " Announces New Product Line",
            "Market Volatility Impacts " + symbol + " Stock",
            symbol + " Expands into International Markets",
            "CEO of " + symbol + " Discusses Future Strategy",
            symbol + " Stock Surges on Positive Outlook",
            "Investors Show Increased Interest in " + symbol
        };
        
        String[] sources = {"Reuters", "Bloomberg", "CNBC", "MarketWatch", "Yahoo Finance"};
        String[] sentiments = {"positive", "neutral", "negative", "positive", "neutral"};
        
        for (int i = 0; i < 5; i++) {
            news.add(MarketNews.builder()
                    .id("mock-news-" + i + "-" + System.currentTimeMillis())
                    .title(headlines[i % headlines.length])
                    .summary("This is a mock news summary for " + symbol + ". In a real implementation, this would contain actual financial news content for AI RAG ingestion.")
                    .url("https://example.com/news/" + symbol + "/" + i)
                    .source(sources[i % sources.length])
                    .publishedAt(LocalDateTime.now().minusHours(i))
                    .symbols(List.of(symbol))
                    .sentiment(sentiments[i % sentiments.length])
                    .relevanceScore(0.7 + (random.nextDouble() * 0.3))
                    .build());
        }
        
        return news;
    }

    public HistoricalData getMockHistoricalData(String symbol, String interval, String outputSize) {
        log.info("Returning mock historical data for symbol: {}, interval: {}", symbol, interval);
        
        List<HistoricalData.PricePoint> pricePoints = new ArrayList<>();
        int days = outputSize.equals("full") ? 365 : 30;
        double basePrice = getBasePriceForSymbol(symbol);
        
        for (int i = 0; i < days; i++) {
            LocalDate date = LocalDate.now().minusDays(i);
            double price = basePrice + (random.nextDouble() * 20 - 10);
            double open = price + (random.nextDouble() * 2 - 1);
            double high = price + random.nextDouble() * 3;
            double low = price - random.nextDouble() * 3;
            long volume = random.nextInt(10000000) + 1000000;
            
            pricePoints.add(HistoricalData.PricePoint.builder()
                    .date(date)
                    .open(BigDecimal.valueOf(open).setScale(2, BigDecimal.ROUND_HALF_UP))
                    .high(BigDecimal.valueOf(high).setScale(2, BigDecimal.ROUND_HALF_UP))
                    .low(BigDecimal.valueOf(low).setScale(2, BigDecimal.ROUND_HALF_UP))
                    .close(BigDecimal.valueOf(price).setScale(2, BigDecimal.ROUND_HALF_UP))
                    .volume(volume)
                    .build());
        }
        
        return HistoricalData.builder()
                .symbol(symbol)
                .pricePoints(pricePoints)
                .interval(interval)
                .outputSize(outputSize)
                .build();
    }

    private double getBasePriceForSymbol(String symbol) {
        return switch (symbol.toUpperCase()) {
            case "AAPL" -> 175.50;
            case "GOOGL", "GOOG" -> 135.00;
            case "MSFT" -> 375.00;
            case "AMZN" -> 145.00;
            case "TSLA" -> 220.00;
            case "META" -> 350.00;
            case "NVDA" -> 450.00;
            case "JPM" -> 155.00;
            case "V" -> 250.00;
            case "JNJ" -> 160.00;
            default -> 100.00 + (random.nextDouble() * 200);
        };
    }

    private String getCompanyNameForSymbol(String symbol) {
        return switch (symbol.toUpperCase()) {
            case "AAPL" -> "Apple Inc.";
            case "GOOGL", "GOOG" -> "Alphabet Inc.";
            case "MSFT" -> "Microsoft Corporation";
            case "AMZN" -> "Amazon.com Inc.";
            case "TSLA" -> "Tesla Inc.";
            case "META" -> "Meta Platforms Inc.";
            case "NVDA" -> "NVIDIA Corporation";
            case "JPM" -> "JPMorgan Chase & Co.";
            case "V" -> "Visa Inc.";
            case "JNJ" -> "Johnson & Johnson";
            default -> symbol + " Corporation";
        };
    }
}
