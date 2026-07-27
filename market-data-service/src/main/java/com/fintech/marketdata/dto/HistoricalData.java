package com.fintech.marketdata.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricalData {

    private String symbol;
    private List<PricePoint> pricePoints;
    private String interval;
    private String outputSize;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PricePoint {
        private LocalDate date;
        private BigDecimal open;
        private BigDecimal high;
        private BigDecimal low;
        private BigDecimal close;
        private Long volume;
    }
}
