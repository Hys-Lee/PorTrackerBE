package com.PorTracker.PorTrackerBE.domain.statistic.dto;

import com.PorTracker.PorTrackerBE.domain.statistic.entity.GroupStatisticRecord;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupStatisticResponse {
    private final Long id;
    private final String statType;
    private final String period;
    private final Integer sampleCount;
    private final Long sumAmountBp;
    private final OffsetDateTime lastUpdatedAt;

    public static GroupStatisticResponse from(GroupStatisticRecord record) {
        return GroupStatisticResponse.builder()
                .id(record.getId())
                .statType(record.getStatType())
                .period(record.getPeriod())
                .sampleCount(record.getSampleCount())
                .sumAmountBp(record.getSumAmountBp())
                .lastUpdatedAt(record.getLastUpdatedAt())
                .build();
    }
}
