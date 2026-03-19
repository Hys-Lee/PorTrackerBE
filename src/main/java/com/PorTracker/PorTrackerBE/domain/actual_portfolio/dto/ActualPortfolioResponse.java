package com.PorTracker.PorTrackerBE.domain.actual_portfolio.dto;

import com.PorTracker.PorTrackerBE.domain.actual_portfolio.entity.ActualPortfolioRecord;
import com.PorTracker.PorTrackerBE.domain.actual_portfolio.entity.TransactionType;
import com.fasterxml.jackson.annotation.JsonProperty;

public record ActualPortfolioResponse(
        @JsonProperty("id") String publicId,
        // Long assetId,
        @JsonProperty("assetId") String assetPublicId,
        String date,
        String createdAt,
        TransactionType transactionType,
        @JsonProperty("currencyId") String currencyPublicId,
        // Long currencyId,
        Long priceBp,
        Long amountBp,
        Long exchangeRateBp) {

    public static ActualPortfolioResponse from(ActualPortfolioRecord record) {
        return new ActualPortfolioResponse(
                record.getPublicId(),
                // record.getAssetId(),
                record.getAssetPublicId(),
                record.getDate(),
                record.getCreatedAt(),
                record.getTransactionType() != null
                        ? TransactionType.from(record.getTransactionType())
                        : null,
                // record.getCurrencyId(),
                record.getCurrencyPublicId(),
                record.getPriceBp(),
                record.getAmountBp(),
                record.getExchangeRateBp());
    }
}
