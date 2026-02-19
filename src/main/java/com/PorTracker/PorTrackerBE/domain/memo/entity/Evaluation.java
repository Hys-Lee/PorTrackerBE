package com.PorTracker.PorTrackerBE.domain.memo.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Evaluation {
    BETTER("better"),
    GOOD("good"),
    SOSO("soso"),
    BAD("bad"),
    WORSE("worse");

    private final String value;

    Evaluation (String value){this.value = value;}

    @JsonValue // json으로 나갈 때 이 값 사용
    public String getValue(){return value;}

    @JsonCreator // json에ㅓㅅ 들어올 때
    public static Evaluation from (String value){
        for (Evaluation evaluation :Evaluation.values()){
            if(evaluation.value.equalsIgnoreCase(value)){
                return evaluation;
            }
        }
        return SOSO ; // 기본값
    }
    
}
