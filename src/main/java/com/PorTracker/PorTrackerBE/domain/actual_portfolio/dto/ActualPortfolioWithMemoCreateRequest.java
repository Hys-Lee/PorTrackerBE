package com.PorTracker.PorTrackerBE.domain.actual_portfolio.dto;

import com.PorTracker.PorTrackerBE.global.constant.ValidationConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActualPortfolioWithMemoCreateRequest extends ActualPortfolioCreateRequest {
    @JsonProperty("memoId")
    @Pattern(regexp = ValidationConstants.UUID_REGEXP, message = "유효한 ID 형식이 아닙니다.")
    private String memoId;
}
