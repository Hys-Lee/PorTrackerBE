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
        @JsonProperty("actualId") String actualPublicId,
        @JsonProperty("targetId") String targetPublicId) {

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
                record.getActualPublicId(),
                record.getTargetPublicId());
    }
}
