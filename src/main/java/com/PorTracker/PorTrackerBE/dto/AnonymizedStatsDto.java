package com.PorTracker.PorTrackerBE.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AnonymizedStatsDto(@JsonProperty("hashed_id") String hashedId,
                @JsonProperty("year_month") String yearMonth,
                @JsonProperty("category") String category,
                @JsonProperty("total_amount") Long totalAmount,

                // 그룹 통계 위한 비식별 메타 데이터
                @JsonProperty("age_group") String ageGroup,
                @JsonProperty("job_type") String jobType) {
}
