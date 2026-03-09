package com.PorTracker.PorTrackerBE.domain.tag.entity;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TagRecord {
    private final Long id;
    private final String publicId;
    private final String content;
}
