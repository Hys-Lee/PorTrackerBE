package com.PorTracker.PorTrackerBE.domain.target_portfolio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TargetPortfolioCreateRequest {
    @JsonProperty("name")
    private String name;

    @JsonProperty("date")
    private String date;

    @JsonProperty("items")
    private List<TargetPortfolioItemRequest> items;
}
