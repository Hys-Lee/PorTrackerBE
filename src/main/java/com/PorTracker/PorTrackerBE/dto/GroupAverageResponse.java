package com.PorTracker.PorTrackerBE.dto;

// import com.fasterxml.jackson.annotation.JsonProperty;

public record GroupAverageResponse(
        // @JsonProperty("year_month") String yearMonth,
        // @JsonProperty("category") String category, @JsonProperty("age_group") String
        // ageGroup,
        // @JsonProperty("job_type") String jobType, @JsonProperty("avg_amount") Long
        // ageAmount,
        // @JsonProperty("contributor_count") Integer contributorCount

        String yeareMonth,
        String category,
        String ageGroup,
        String jobType,
        Long avgAmount,
        Integer contributorCount) {}
