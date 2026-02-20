package com.PorTracker.PorTrackerBE.domain.asset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssetTypeRequest {
    @NotBlank(message = "이름이 없습니다.")
    @Size(max = 20, message = "최대 글자수 20자를 초과했습니다.")
    private String name;
}
