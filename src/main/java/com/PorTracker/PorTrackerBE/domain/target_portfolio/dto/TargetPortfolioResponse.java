package com.PorTracker.PorTrackerBE.domain.target_portfolio.dto;

import com.PorTracker.PorTrackerBE.domain.target_portfolio.entity.TargetPortfolioItemRecord;
import com.PorTracker.PorTrackerBE.domain.target_portfolio.entity.TargetPortfolioRecord;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record TargetPortfolioResponse(@JsonProperty("id") String publicId, String name, String date,
        String createdAt, List<TargetPortfolioItemResponse> items) {

    public static TargetPortfolioResponse from(TargetPortfolioRecord record,
            List<TargetPortfolioItemRecord> items) {
        List<TargetPortfolioItemResponse> itemResponses =
                items.stream().map(TargetPortfolioItemResponse::from).toList();
        return new TargetPortfolioResponse(record.getPublicId(), record.getName(), record.getDate(),
                record.getCreatedAt(), itemResponses);
    }

    public record TargetPortfolioItemResponse(String assetId, Long currentRatioBp,
            Long ratioDeltaBp) {

        public static TargetPortfolioItemResponse from(TargetPortfolioItemRecord item) {
            return new TargetPortfolioItemResponse(item.getAssetPublicId(),
                    item.getCurrentRatioBp(), item.getRatioDeltaBp());
        }
    }
}
