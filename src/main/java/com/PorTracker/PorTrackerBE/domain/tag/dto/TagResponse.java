package com.PorTracker.PorTrackerBE.domain.tag.dto;

import com.PorTracker.PorTrackerBE.domain.tag.entity.TagRecord;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TagResponse {
    private final Long id;
    private final String content;

    public static TagResponse from(TagRecord record) {
        return TagResponse.builder()
                .id(record.getId())
                .content(record.getContent())
                .build();
    }
}
