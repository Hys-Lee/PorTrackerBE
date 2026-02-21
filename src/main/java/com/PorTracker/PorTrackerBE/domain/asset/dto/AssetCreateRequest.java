package com.PorTracker.PorTrackerBE.domain.asset.dto;

import com.PorTracker.PorTrackerBE.global.constant.ValidationConstants;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssetCreateRequest {
    @NotBlank(message = "이름이 없습니다.")
    @Size(max = 20, message = "최대 글자 수 20자를 초과합니다.")
    private String name;

    @Size(max = 40, message = "최대 글자수 40자를 초과합니다.")
    private String description;

    @JsonProperty("currencyId") // JSON의 "currencyId" 키를 이 필드에 매핑
    @NotBlank(message = "대상 통화가 지정되지 않았습니다.")
    @Pattern(regexp = ValidationConstants.UUID_REGEXP, message = "유효한 ID 형식이 아닙니다.")
    private String currencyId;

    @JsonProperty("typeId") // JSON의 "typeId" 키를 이 필드에 매핑
    @NotBlank(message = "자산 타입이 지정되지 않았습니다.")
    @Pattern(regexp = ValidationConstants.UUID_REGEXP, message = "유효한 ID 형식이 아닙니다.")
    private String typeId;
}
