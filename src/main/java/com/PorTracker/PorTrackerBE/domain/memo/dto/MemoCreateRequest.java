package com.PorTracker.PorTrackerBE.domain.memo.dto;

import com.PorTracker.PorTrackerBE.domain.memo.entity.Evaluation;
import com.PorTracker.PorTrackerBE.domain.memo.entity.Importance;
import com.PorTracker.PorTrackerBE.domain.memo.entity.MemoType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemoCreateRequest {
    private String title;
    private String content;
    // private String importance;
    private Importance importance;
    // private String evaluation;
    private Evaluation evaluation;
    private String date;

    @JsonProperty("memoType")
    // private String memoType;
    private MemoType memoType;

    @JsonProperty("actualId") // JSON의 "actualId" 키를 이 필드에 매핑
    private String actualId;

    @JsonProperty("targetId") // JSON의 "targetId" 키를 이 필드에 매핑
    private String targetId;
}
