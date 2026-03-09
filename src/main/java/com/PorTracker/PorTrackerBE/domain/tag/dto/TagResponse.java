package com.PorTracker.PorTrackerBE.domain.tag.dto;

import com.PorTracker.PorTrackerBE.domain.tag.entity.TagRecord;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TagResponse {
    @com.fasterxml.jackson.annotation.JsonProperty("id")
    private final String publicId;

    private final String content;

    public static TagResponse from(TagRecord record) {
        return TagResponse.builder()
                .publicId(record.getPublicId())
                .content(record.getContent())
                .build();
    }
}
