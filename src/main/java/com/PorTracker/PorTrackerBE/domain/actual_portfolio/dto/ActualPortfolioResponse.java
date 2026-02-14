package com.PorTracker.PorTrackerBE.domain.actual_portfolio.dto;

import com.PorTracker.PorTrackerBE.domain.actual_portfolio.entity.ActualPortfolioRecord;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ActualPortfolioResponse(@JsonProperty("id") String publicId, Long assetId,
        String date, String transactionType, Long currencyId, Long priceBp, Long amountBp,
        Long exchangeRateBp) {

    public static ActualPortfolioResponse from(ActualPortfolioRecord record) {
        return new ActualPortfolioResponse(record.getPublicId(), record.getAssetId(),
                record.getDate(), record.getTransactionType(), record.getCurrencyId(),
                record.getPriceBp(), record.getAmountBp(), record.getExchangeRateBp());
    }
}
