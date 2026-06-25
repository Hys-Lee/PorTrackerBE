package com.PorTracker.PorTrackerBE.domain.profile.entity;

import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileRecord {
    private final UUID id;
    private final String email;
    private final String role;
    private final String nickname;
    private final Long baseCurrencyId;
    private final int userDbVersion;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;
}
