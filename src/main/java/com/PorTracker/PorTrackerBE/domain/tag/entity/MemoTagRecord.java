package com.PorTracker.PorTrackerBE.domain.tag.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemoTagRecord {
    private final Long memoId;
    private final Long tagId;
    private final String createdAt;
    private final String updatedAt;
    private final String deletedAt;
}
