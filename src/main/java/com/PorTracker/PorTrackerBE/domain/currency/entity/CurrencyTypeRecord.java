package com.PorTracker.PorTrackerBE.domain.currency.entity;

// import lombok.Builder;
// import lombok.Getter;

// @Getter
// @Builder
// public class CurrencyTypeRecord {
// private final Long id;
// private final String code;
// }

public record CurrencyTypeRecord(Long id, String publicId, String code) {}
