package com.PorTracker.PorTrackerBE.domain.profile.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileRecord {
    private final String id;
    private final String email;
    private final String role;
    private final String nickname;
    private final Long baseCurrencyId;
    private final String createdAt;
    private final String updatedAt;
}
