package com.PorTracker.PorTrackerBE.domain.target_portfolio.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TargetPortfolioSnapshotRecord {
    private final Long id;
    private final Long portfolioId;
    private final String createdAt;
}
