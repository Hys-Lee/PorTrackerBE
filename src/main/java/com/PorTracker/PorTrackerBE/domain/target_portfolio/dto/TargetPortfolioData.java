package com.PorTracker.PorTrackerBE.domain.target_portfolio.dto;

import com.PorTracker.PorTrackerBE.domain.target_portfolio.entity.TargetPortfolioItemRecord;
import com.PorTracker.PorTrackerBE.domain.target_portfolio.entity.TargetPortfolioRecord;
import java.util.List;

public record TargetPortfolioData(
        TargetPortfolioRecord portfolio, List<TargetPortfolioItemRecord> items) {}
