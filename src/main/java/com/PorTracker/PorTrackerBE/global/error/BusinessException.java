package com.PorTracker.PorTrackerBE.global.error;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final ErrorCode errorCode;
    private final String detail;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = null;
    }

    public BusinessException(ErrorCode errorCode, String detail) {
        super(
                detail != null
                        ? errorCode.getMessage() + "(" + detail + ")"
                        : errorCode.getMessage());
        this.errorCode = errorCode;
        this.detail = detail;
    }
}
