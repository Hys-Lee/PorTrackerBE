package com.PorTracker.PorTrackerBE.domain.memo.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum MemoType {
    ACTUAL("actual"),
    TARGET("target"),
    EVENT("event");

    private final String value;

    MemoType (String value){this.value = value;}

    @JsonValue // json으로 나갈 때 이 값 사용
    public String getValue(){return value;}

    @JsonCreator // json에ㅓㅅ 들어올 때
    public static MemoType from (String value){
        for (MemoType memoType :MemoType.values()){
            if(memoType.value.equalsIgnoreCase(value)){
                return memoType;
            }
        }
        return EVENT ; // 기본값
    }

}
