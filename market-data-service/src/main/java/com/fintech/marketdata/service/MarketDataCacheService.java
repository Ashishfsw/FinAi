package com.fintech.marketdata.service;

import com.fintech.marketdata.dto.HistoricalData;
import com.fintech.marketdata.dto.MarketNews;
import com.fintech.marketdata.dto.StockQuote;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class MarketDataCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ExternalMarketDataService externalMarketDataService;

    private static final String QUOTE_KEY_PREFIX = "quote:";
    private static final String HISTORICAL_KEY_PREFIX = "historical:";
    private static final String NEWS_KEY_PREFIX = "news:";
    private static final long DEFAULT_TTL_MINUTES = 5;

    public Mono<StockQuote> getStockQuoteWithCache(String symbol) {
        String cacheKey = QUOTE_KEY_PREFIX + symbol;
        
        StockQuote cached = (StockQuote) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("Cache hit for stock quote: {}", symbol);
            return Mono.just(cached);
        }

        log.debug("Cache miss for stock quote: {}, fetching from external API", symbol);
        return externalMarketDataService.getStockQuote(symbol)
                .doOnNext(quote -> {
                    if (quote != null) {
                        redisTemplate.opsForValue().set(cacheKey, quote, DEFAULT_TTL_MINUTES, TimeUnit.MINUTES);
                    }
                });
    }

    public Mono<HistoricalData> getHistoricalDataWithCache(String symbol, String interval, String outputSize) {
        String cacheKey = HISTORICAL_KEY_PREFIX + symbol + ":" + interval + ":" + outputSize;
        
        HistoricalData cached = (HistoricalData) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.debug("Cache hit for historical data: {}", symbol);
            return Mono.just(cached);
        }

        log.debug("Cache miss for historical data: {}, fetching from external API", symbol);
        return externalMarketDataService.getHistoricalData(symbol, interval, outputSize)
                .doOnNext(data -> {
                    if (data != null) {
                        redisTemplate.opsForValue().set(cacheKey, data, DEFAULT_TTL_MINUTES, TimeUnit.MINUTES);
                    }
                });
    }

    public Mono<List<MarketNews>> getMarketNewsWithCache(String symbol) {
        String cacheKey = NEWS_KEY_PREFIX + symbol;
        
        @SuppressWarnings("unchecked")
        List<MarketNews> cached = (List<MarketNews>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null && !cached.isEmpty()) {
            log.debug("Cache hit for market news: {}", symbol);
            return Mono.just(cached);
        }

        log.debug("Cache miss for market news: {}, fetching from external API", symbol);
        return externalMarketDataService.getMarketNews(symbol)
                .doOnNext(news -> {
                    if (news != null && !news.isEmpty()) {
                        redisTemplate.opsForValue().set(cacheKey, news, DEFAULT_TTL_MINUTES, TimeUnit.MINUTES);
                    }
                });
    }

    public void evictSymbolCache(String symbol) {
        redisTemplate.delete(QUOTE_KEY_PREFIX + symbol);
        redisTemplate.delete(NEWS_KEY_PREFIX + symbol);
        log.info("Evicted cache for symbol: {}", symbol);
    }

    public void evictAllCache() {
        redisTemplate.delete(redisTemplate.keys(QUOTE_KEY_PREFIX + "*"));
        redisTemplate.delete(redisTemplate.keys(HISTORICAL_KEY_PREFIX + "*"));
        redisTemplate.delete(redisTemplate.keys(NEWS_KEY_PREFIX + "*"));
        log.info("Evicted all market data cache");
    }
}
