package com.fintech.marketdata.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketNews {

    private String id;
    private String title;
    private String summary;
    private String url;
    private String source;
    private LocalDateTime publishedAt;
    private List<String> symbols;
    private String sentiment;
    private Double relevanceScore;
}
