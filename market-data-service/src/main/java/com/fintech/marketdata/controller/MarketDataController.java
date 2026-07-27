package com.fintech.marketdata.controller;

import com.fintech.marketdata.dto.HistoricalData;
import com.fintech.marketdata.dto.MarketNews;
import com.fintech.marketdata.dto.StockQuote;
import com.fintech.marketdata.service.MarketDataCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class MarketDataController {

    private final MarketDataCacheService marketDataCacheService;

    // New API v1 endpoints as requested
    @GetMapping("/market/price/{ticker}")
    public Mono<ResponseEntity<StockQuote>> getLivePrice(@PathVariable String ticker) {
        return marketDataCacheService.getStockQuoteWithCache(ticker.toUpperCase())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/market/news/{ticker}")
    public Mono<ResponseEntity<List<MarketNews>>> getMarketNews(@PathVariable String ticker) {
        return marketDataCacheService.getMarketNewsWithCache(ticker.toUpperCase())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    // Legacy endpoints for backward compatibility
    @GetMapping("/market-data/quote/{symbol}")
    public Mono<ResponseEntity<StockQuote>> getStockQuote(@PathVariable String symbol) {
        return marketDataCacheService.getStockQuoteWithCache(symbol.toUpperCase())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/market-data/historical/{symbol}")
    public Mono<ResponseEntity<HistoricalData>> getHistoricalData(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "daily") String interval,
            @RequestParam(defaultValue = "compact") String outputSize) {
        return marketDataCacheService.getHistoricalDataWithCache(symbol.toUpperCase(), interval, outputSize)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/market-data/news/{symbol}")
    public Mono<ResponseEntity<List<MarketNews>>> getLegacyMarketNews(@PathVariable String symbol) {
        return marketDataCacheService.getMarketNewsWithCache(symbol.toUpperCase())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/market-data/cache/{symbol}")
    public ResponseEntity<Void> evictSymbolCache(@PathVariable String symbol) {
        marketDataCacheService.evictSymbolCache(symbol.toUpperCase());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/market-data/cache")
    public ResponseEntity<Void> evictAllCache() {
        marketDataCacheService.evictAllCache();
        return ResponseEntity.noContent().build();
    }
}
