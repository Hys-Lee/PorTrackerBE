package com.PorTracker.PorTrackerBE.domain.memo.dto;

import com.PorTracker.PorTrackerBE.domain.memo.entity.Evaluation;
import com.PorTracker.PorTrackerBE.domain.memo.entity.Importance;
import com.PorTracker.PorTrackerBE.domain.memo.entity.MemoType;
import com.PorTracker.PorTrackerBE.global.constant.ValidationConstants;
import com.PorTracker.PorTrackerBE.global.util.FlexibleOffsetDateTimeDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MemoCreateRequest {
    @NotBlank(message = "제목이 없습니다.")
    @Size(max = 20, message = "제목 최대 글자수 20을 넘겼습니다.")
    private String title;

    @Size(max = 1000, message = "내용 최대 글자수 1000을 넘겼습니다.")
    private String content;

    // private String importance;
    @NotNull(message = "중요도가 선택되지 않았습니다.")
    private Importance importance;

    // private String evaluation;

    private Evaluation evaluation;

    @NotNull(message = "날짜가 없습니다.")
    // @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) // ISO 허용
    @JsonDeserialize(using = FlexibleOffsetDateTimeDeserializer.class)
    private String date;

    // private String memoType;
    @JsonProperty("memoType")
    @NotNull(message = "메모 타입을 정하지 않았습니다.")
    private MemoType memoType;

    @JsonProperty("actualId") // JSON의 "actualId" 키를 이 필드에 매핑
    @Pattern(regexp = ValidationConstants.UUID_REGEXP, message = "유효한 ID 형식이 아닙니다.")
    private String actualId;

    @JsonProperty("targetId") // JSON의 "targetId" 키를 이 필드에 매핑
    @Pattern(regexp = ValidationConstants.UUID_REGEXP, message = "유효한 ID 형식이 아닙니다.")
    private String targetId;
}
