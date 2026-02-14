package com.PorTracker.PorTrackerBE.domain.asset.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AssetCreateRequest {
    private String name;
    private String description;

    @JsonProperty("currencyId") // JSON의 "currencyId" 키를 이 필드에 매핑
    private String currencyId;

    @JsonProperty("typeId") // JSON의 "typeId" 키를 이 필드에 매핑
    private String typeId;
}
