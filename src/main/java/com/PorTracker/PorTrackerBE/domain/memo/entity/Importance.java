package com.PorTracker.PorTrackerBE.domain.memo.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Importance {
    CRITICAL("critical"),
    USEFUL("useful"),
    NORMAL("normal");

    private final String value;

    Importance(String value) {
        this.value = value;
    }

    @JsonValue // json으로 나갈 때 이 값 사용
    public String getValue() {
        return value;
    }

    @JsonCreator // json에ㅓㅅ 들어올 때
    public static Importance from(String value) {
        for (Importance importance : Importance.values()) {
            if (importance.value.equalsIgnoreCase(value)) {
                return importance;
            }
        }
        // return NORMAL ; // 기본값
        // throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,"유효하지 않은 중요도 값입니다.");
        throw new IllegalArgumentException(value);
    }
}
