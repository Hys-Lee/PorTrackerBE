package com.PorTracker.PorTrackerBE.global.error;

import lombok.Getter;

@Getter
public enum ErrorCode {
    // 공통
    INVALID_INPUT_VALUE(400, "C001", " 올바르지 않은 입력값입니다."),
    INTERNAL_SERVER_ERROR(500, "C002", " 서버 내부 오류가 발생했습니다."),
    TOO_MANY_REQUESTS(429, "C003", "잠시 후 다시 시도해주세요. 현재 요청이 처리 중입니다."),

    // 구글 시트
    SHEET_NOT_FOUND(404, "S001", " 구글 시트를 찾을 수 없습니다."),
    INVALID_ID_ACCESS_TOKEN(401, "S002", " 유효하지 않은 구글 엑세스 토큰입니다."),

    // DB
    DATA_SAVE_FAILED(500, "D001", " 데이터 저장 중 오류가 발생했습니다."),
    NO_DATA(404, "D002", "해당 데이터는 존재하지 않습니다."),

    // Google Drive File
    FILE_DOWNLOAD_FAILED(500, "F001", "구글 드라이브 파일 다운로드에 실패했습니다."),

    DB_SYNC_FAILED(500, "F002", "DB 동기화 중 오류가 발생했습니다");

    private final int status;
    private final String code;
    private final String message;

    ErrorCode(int status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
