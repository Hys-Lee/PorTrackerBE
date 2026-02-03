package com.PorTracker.PorTrackerBE.dto;

public record TransactionDto(String date, String category, String item, Long amount, String memo) {
}
