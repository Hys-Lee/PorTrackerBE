package com.PorTracker.PorTrackerBE.domain.target_portfolio.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TargetPortfolioItemRecord {
    private final Long assetId;
    private final Long snapshotId;
    private final Long currencRateBp;
    private final Long ratioDeltaBp;

}
