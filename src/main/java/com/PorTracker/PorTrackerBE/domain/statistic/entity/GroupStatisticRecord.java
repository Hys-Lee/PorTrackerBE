package com.PorTracker.PorTrackerBE.domain.statistic.entity;

import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class GroupStatisticRecord {
    private final Long id;
    private final String statType;
    private final String period;
    private final Integer sampleCount;
    private final Long sumAmountBp;
    private final OffsetDateTime lastUpdatedAt;
}