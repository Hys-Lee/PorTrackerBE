package com.PorTracker.PorTrackerBE.domain.target_portfolio.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TargetPortfolioSearchRequest {
    private List<String> names;
    private String startDate;
    private String endDate;
    private Integer limit = 5;
    private Integer offset = 0;
}
