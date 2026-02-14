package com.PorTracker.PorTrackerBE.domain.target_portfolio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TargetPortfolioItemRequest {
    @JsonProperty("assetId")
    private String assetId;

    @JsonProperty("currentRatioBp")
    private Long currentRatioBp;

    @JsonProperty("ratioDeltaBp")
    private Long ratioDeltaBp;
}
