package com.PorTracker.PorTrackerBE.domain.currency.dto;

import com.PorTracker.PorTrackerBE.domain.currency.entity.CurrencyTypeRecord;
import com.fasterxml.jackson.annotation.JsonProperty;

public record CurrencyTypeResponse(@JsonProperty("id") String publicId, String code) {
    public static CurrencyTypeResponse from(CurrencyTypeRecord record) {
        return new CurrencyTypeResponse(record.publicId(), record.code());
    }
}
