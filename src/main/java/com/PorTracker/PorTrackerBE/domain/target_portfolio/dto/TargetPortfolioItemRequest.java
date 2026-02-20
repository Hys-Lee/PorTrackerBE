package com.PorTracker.PorTrackerBE.domain.target_portfolio.dto;


import com.PorTracker.PorTrackerBE.global.constant.ValidationConstants;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
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
    @NotBlank(message = "대상이 없습니다.")
    @Pattern(regexp = ValidationConstants.UUID_REGEXP,message = "유효한 ID 형식이 아닙니다.")
    private String assetId;

    @JsonProperty("currentRatioBp")
    @PositiveOrZero(message = "0이상의 정수가 아닙니다.")
    @NotNull(message = "base point 값이 없습니다.")
    private Long currentRatioBp;

    // @JsonProperty("ratioDeltaBp")
    // private Long ratioDeltaBp;
}
