package com.PorTracker.PorTrackerBE.dto;

public record AnonymizedStatsDto(
        // @JsonProperty() String hashedId,
        // @JsonProperty("year_month") String yearMonth,
        // @JsonProperty("category") String category,
        // @JsonProperty("total_amount") Long totalAmount,

        // // 그룹 통계 위한 비식별 메타 데이터
        // @JsonProperty("age_group") String ageGroup,
        // @JsonProperty("job_type") String jobType

        String hashedId,
        String yearMonth,
        String category,
        Long totalAmount,
        String ageGroup,
        String jobType) {}
