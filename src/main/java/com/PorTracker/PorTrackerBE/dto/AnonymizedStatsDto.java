package com.PorTracker.PorTrackerBE.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AnonymizedStatsDto(@JsonProperty("hashed_id") String hashedId,
                @JsonProperty("year_month") String yearMonth,
                @JsonProperty("category") String category,
                @JsonProperty("total_amount") Long totalAmount) {
}
