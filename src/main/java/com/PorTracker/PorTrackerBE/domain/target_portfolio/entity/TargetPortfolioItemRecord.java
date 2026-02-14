package com.PorTracker.PorTrackerBE.domain.target_portfolio.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TargetPortfolioItemRecord {
    private final Long assetId;
    private final String assetPublicId;
    private final Long snapshotId;
    private final Long currentRatioBp;
    private final Long ratioDeltaBp;
}
