package com.PorTracker.PorTrackerBE.domain.asset.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AssetRecord {
    private final Long id;
    private final String publicId;
    private final String createdAt;
    private final String updatedAt;
    private final String deletedAt;

    private final String name;
    private final String description;
    private final Long currencyId;
    private final Long typeId;

}
