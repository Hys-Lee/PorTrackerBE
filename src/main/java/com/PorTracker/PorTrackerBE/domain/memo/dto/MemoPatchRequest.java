package com.PorTracker.PorTrackerBE.domain.memo.dto;

import com.PorTracker.PorTrackerBE.global.constant.ValidationConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemoPatchRequest {
    @JsonProperty("actualId")
    @Pattern(regexp = ValidationConstants.UUID_REGEXP, message = "유효한 ID 형식이 아닙니다.")
    private String actualId;

    @JsonProperty("targetId")
    @Pattern(regexp = ValidationConstants.UUID_REGEXP, message = "유효한 ID 형식이 아닙니다.")
    private String targetId;
}
