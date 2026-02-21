package com.PorTracker.PorTrackerBE.global.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class FlexibleOffsetDateTimeDeserializer extends JsonDeserializer<OffsetDateTime> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    @Override
    public OffsetDateTime deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException {
        String text = p.getText().trim();
        if (text.isEmpty()) return null;
        try {
            // ISO 8601 형식
            if (text.contains("+") || text.endsWith("Z") || (text.split("-").length > 3)) {
                return OffsetDateTime.parse(text);
            }

            // 날짜 + 시간만
            if (text.contains("T")) {
                return LocalDateTime.parse(text).atZone(ZoneId.systemDefault()).toOffsetDateTime();
            }

            // 날짜만
            return LocalDate.parse(text).atStartOfDay(ZoneId.systemDefault()).toOffsetDateTime();
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(text);
        }
    }
}
