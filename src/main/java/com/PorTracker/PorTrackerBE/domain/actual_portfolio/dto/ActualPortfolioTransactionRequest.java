package com.PorTracker.PorTrackerBE.domain.actual_portfolio.dto;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ActualPortfolioTransactionRequest {
    private String assetPublicId;
    private LocalDateTime date;
    private String transactionType;
    private Long currencyPublicId;
    private Long priceBp;
    private Long amountBp;
    private Long exchangeRateBp;
}
