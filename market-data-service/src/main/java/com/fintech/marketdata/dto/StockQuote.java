package com.fintech.marketdata.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockQuote {

    private String symbol;
    private String name;
    private BigDecimal price;
    private BigDecimal change;
    private BigDecimal changePercent;
    private BigDecimal open;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal previousClose;
    private Long volume;
    private LocalDateTime timestamp;
    private String exchange;

    @JsonProperty("01. symbol")
    private String avSymbol;

    @JsonProperty("02. open")
    private String avOpen;

    @JsonProperty("03. high")
    private String avHigh;

    @JsonProperty("04. low")
    private String avLow;

    @JsonProperty("05. price")
    private String avPrice;

    @JsonProperty("06. volume")
    private String avVolume;

    @JsonProperty("07. latest trading day")
    private String avLatestTradingDay;

    @JsonProperty("08. previous close")
    private String avPreviousClose;

    @JsonProperty("09. change")
    private String avChange;

    @JsonProperty("10. change percent")
    private String avChangePercent;
}
