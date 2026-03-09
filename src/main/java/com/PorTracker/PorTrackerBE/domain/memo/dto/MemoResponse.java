package com.PorTracker.PorTrackerBE.domain.memo.dto;

import com.PorTracker.PorTrackerBE.domain.memo.entity.Evaluation;
import com.PorTracker.PorTrackerBE.domain.memo.entity.Importance;
import com.PorTracker.PorTrackerBE.domain.memo.entity.MemoRecord;
import com.PorTracker.PorTrackerBE.domain.memo.entity.MemoType;
import com.fasterxml.jackson.annotation.JsonProperty;

public record MemoResponse(
        @JsonProperty("id") String publicId,
        String createdAt,
        Importance importance,
        String title,
        String content,
        Evaluation evaluation,
        String date,
        MemoType memoType,
        @JsonProperty("actualId") String actualPublicId,
        @JsonProperty("targetId") String targetPublicId,
        @JsonProperty("tags") java.util.List<String> tags) {

    public static MemoResponse from(MemoRecord record) {
        return new MemoResponse(
                record.getPublicId(),
                record.getCreatedAt(),
                record.getImportance() != null ? Importance.from(record.getImportance()) : null,
                record.getTitle(),
                record.getContent(),
                record.getEvaluation() != null ? Evaluation.from(record.getEvaluation()) : null,
                record.getDate(),
                record.getMemoType() != null ? MemoType.from(record.getMemoType()) : null,
                record.getActualPublicId(),
                record.getTargetPublicId(),
                record.getTags() != null ? record.getTags() : java.util.Collections.emptyList());
    }
}
