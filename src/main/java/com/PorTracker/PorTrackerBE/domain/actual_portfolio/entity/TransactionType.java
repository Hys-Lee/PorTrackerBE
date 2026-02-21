package com.PorTracker.PorTrackerBE.domain.actual_portfolio.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TransactionType {
    ALLOCATION("allocation"),
    WITHDRAWAL("withdrawal"),
    DIVIDEND("dividend"),
    FEE("fee");

    private final String value;

    TransactionType(String value) {
        this.value = value;
    }

    @JsonValue // json으로 나갈 때 이 값 사용
    public String getValue() {
        return value;
    }

    @JsonCreator // json에ㅓㅅ 들어올 때
    public static TransactionType from(String value) {
        for (TransactionType transactionType : TransactionType.values()) {
            if (transactionType.value.equalsIgnoreCase(value)) {
                return transactionType;
            }
        }
        // throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE,"유효하지 않은 거래 타입입니다.");
        throw new IllegalArgumentException(value);
    }
}
