package com.PorTracker.PorTrackerBE.domain.memo.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(toBuilder = true)
public class MemoRecord {
    private final Long id;
    private final String publicId;
    private final String createdAt;
    // private final String updatedAt;
    // private final String deletedAt;
    private final String importance;
    private final String title;
    private final String content;
    private final String evaluation;
    private final String date;
    private final String memoType;
    private final Long actualId;
    private final String actualPublicId;
    private final Long targetId;
    private final String targetPublicId;
    private final java.util.List<String> tags;
}
