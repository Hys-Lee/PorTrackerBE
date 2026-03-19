package com.PorTracker.PorTrackerBE.domain.target_portfolio.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.PorTracker.PorTrackerBE.global.constant.ValidationConstants;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TargetPortfolioWithMemoCreateRequest extends TargetPortfolioCreateRequest {
    @JsonProperty("memoId")
    @Pattern(regexp = ValidationConstants.UUID_REGEXP, message = "유효한 ID 형식이 아닙니다.")
    private String memoId;
}
