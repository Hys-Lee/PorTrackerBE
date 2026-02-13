package com.PorTracker.PorTrackerBE.domain.asset.dto;

import com.PorTracker.PorTrackerBE.domain.asset.entity.AssetTypeRecord;
import com.fasterxml.jackson.annotation.JsonProperty;

public record AssetTypeResponse(
        @JsonProperty("id") String publicId, String name, String createdAt) {

    public static AssetTypeResponse from(AssetTypeRecord record) {
        return new AssetTypeResponse(record.getPublicId(), record.getName(), record.getCreatedAt());
    }
}
