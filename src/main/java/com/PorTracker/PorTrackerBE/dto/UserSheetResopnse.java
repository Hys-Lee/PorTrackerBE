package com.PorTracker.PorTrackerBE.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record UserSheetResopnse(
        @JsonProperty("user_id") String userId,
        @JsonProperty("spreadsheet_id") String spreadsheetId,
        @JsonProperty("created_at") String createdAt) {}
