package com.PorTracker.PorTrackerBE.domain.target_portfolio.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TargetPortfolioRecord {
    private final Long id;
    private final String publicId;
    private final String name;
    private final String date;
    private final String createdAt;
    private final String deletedAt;
}
