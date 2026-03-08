package com.PorTracker.PorTrackerBE.global.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "공통 에러 응답 구조")
public class ErrorResponse {
    @Schema(description = "HTTP 상태 코드", example = "400")
    private final int status;

    @Schema(description = "에러 코드", example = "C001")
    private final String code;

    @Schema(description = "에러 메시지", example = "잘못된 입력값입니다.")
    private final String message;

    @Schema(description = "에러 디테일", example = "어디에서 문제가 발생했습니다.")
    private final String detail;

    // ErrorResponse 생성하는 정적 팩토리 메서드 라고 함.
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();
    }

    // detail있는 버전
    public static ErrorResponse of(ErrorCode errorCode, String detail) {
        return ErrorResponse.builder()
                .status(errorCode.getStatus())
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .detail(detail)
                .build();
    }
}
