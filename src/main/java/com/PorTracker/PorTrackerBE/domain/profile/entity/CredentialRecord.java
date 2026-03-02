package com.PorTracker.PorTrackerBE.domain.profile.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CredentialRecord {
    private final UUID id;
    private final String provider;
    private final String accessToken;
    private final String refreshToken;
    private final OffsetDateTime updatedAt;
    // expiresAt인가는 필요 없나?
}
