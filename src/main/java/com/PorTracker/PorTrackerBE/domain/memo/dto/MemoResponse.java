package com.PorTracker.PorTrackerBE.domain.memo.dto;

import com.PorTracker.PorTrackerBE.domain.memo.entity.MemoRecord;
import com.fasterxml.jackson.annotation.JsonProperty;

public record MemoResponse(
        @JsonProperty("id") String publicId,
        String createdAt,
        String importance,
        String title,
        String content,
        String evaluation,
        String date,
        String memoType,
        @JsonProperty("actualId") Long actualPublicId,
        @JsonProperty("targetId") Long targetPublicId) {

    public static MemoResponse from(MemoRecord record) {
        return new MemoResponse(
                record.getPublicId(),
                record.getCreatedAt(),
                record.getImportance(),
                record.getTitle(),
                record.getContent(),
                record.getEvaluation(),
                record.getDate(),
                record.getMemoType(),
                record.getActualId(),
                record.getTargetId());
    }
}
