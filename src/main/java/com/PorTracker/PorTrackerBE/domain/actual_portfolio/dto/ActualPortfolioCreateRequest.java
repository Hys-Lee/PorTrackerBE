package com.PorTracker.PorTrackerBE.domain.actual_portfolio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActualPortfolioCreateRequest {
    @JsonProperty("assetId")
    // private Long assetId;
    private String assetId;

    @JsonProperty("date")
    private String date;

    @JsonProperty("transactionType")
    private String transactionType;

    @JsonProperty("currencyId")
    // private Long currencyId;
    private String currencyId;

    @JsonProperty("priceBp")
    private Long priceBp;

    @JsonProperty("amountBp")
    private Long amountBp;

    @JsonProperty("exchangeRateBp")
    private Long exchangeRateBp;
}
