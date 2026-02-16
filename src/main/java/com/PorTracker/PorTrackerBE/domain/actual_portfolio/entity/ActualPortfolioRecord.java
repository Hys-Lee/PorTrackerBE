package com.PorTracker.PorTrackerBE.domain.actual_portfolio.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
// private final Long id; // autoincrement 되니 제외
public class ActualPortfolioRecord {
    private final Long id;
    private final String publicId;
    private final Long assetId;
    private final String assetPublicId;

    private final String date;
    // private final String createdAt;
    // private final String updatedAt;
    // private final String deletedAt;
    private final String transactionType;
    private final Long changeRatioBp;
    private final Long accumulatedRatioBp;
    private final Long currencyId;
    private final String currencyPublicId;

    private final Long priceBp;
    private final Long amountBp;
    private final Long exchangeRateBp;

    // public static ActualPortfolioRecord fromRequest(ActualPortfolioTransactionRequest dto) {
    // return ActualPortfolioRecord.builder().publicId(UUID.randomUUID().toString())
    // .assetId(dto.getAssetId()).date(dto.getDate().toString())
    // .transactionType(dto.getTransactionType()).currencyId(dto.getCurrencyId())
    // .priceBp(dto.getPriceBp()).amountBp(dto.getAmountBp())
    // .exchangeRateBp(dto.getExchangeRateBp()).build();
    // }
}
