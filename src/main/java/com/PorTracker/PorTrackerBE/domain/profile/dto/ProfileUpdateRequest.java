package com.PorTracker.PorTrackerBE.domain.profile.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor // @RequestBody 직렬화를 위해 필요
public class ProfileUpdateRequest {
    private String nickname;
    private Long baseCurrencyId;
}
