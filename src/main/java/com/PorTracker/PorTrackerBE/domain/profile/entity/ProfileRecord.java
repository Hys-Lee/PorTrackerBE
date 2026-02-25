package com.PorTracker.PorTrackerBE.domain.profile.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class ProfileRecord {
    private final UUID id;
    private final String email;
    private final String role;
    private final String nickname;
    private final Long baseCurrencyId;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;
}