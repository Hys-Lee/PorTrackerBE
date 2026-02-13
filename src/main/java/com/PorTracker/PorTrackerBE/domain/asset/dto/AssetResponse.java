package com.PorTracker.PorTrackerBE.domain.asset.dto;

import com.PorTracker.PorTrackerBE.domain.asset.entity.AssetRecord;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AssetResponse(@JsonProperty("id") String publicId, String name, String description,
        String createdAt, Long currencyId, Long typeId) {

    public static AssetResponse from(AssetRecord record) {
        return new AssetResponse(record.getPublicId(), record.getName(), record.getDescription(),
                record.getCreatedAt(), record.getCurrencyId(), record.getTypeId());
    }
}
