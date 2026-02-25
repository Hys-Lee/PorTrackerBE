package com.PorTracker.PorTrackerBE.domain.profile.dto;

import com.PorTracker.PorTrackerBE.domain.profile.entity.ProfileRecord;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class ProfileResponse {
    private final UUID id;
    private final String email;
    private final String nickname;
    private final Long baseCurrencyId;
    private final OffsetDateTime createdAt;
    private final OffsetDateTime updatedAt;

    public static ProfileResponse from(ProfileRecord record) {
        return ProfileResponse.builder()
                .id(record.getId())
                .email(record.getEmail())
                .nickname(record.getNickname())
                .baseCurrencyId(record.getBaseCurrencyId())
                .createdAt(record.getCreatedAt())
                .updatedAt(record.getUpdatedAt())
                .build();
    }
}