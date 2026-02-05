package com.PorTracker.PorTrackerBE.global.error;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice // 모든 Controller 에러 여기서 가로챔
public class GlobalExceptionHandler {

    // 직접 정의한 BusinessException 에러 핸들링
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.error("BusinessException: {}", e.getErrorCode().getMessage());
        ErrorCode errorCode = e.getErrorCode();
        ErrorResponse response = ErrorResponse.of(errorCode);

        return new ResponseEntity<>(response,
                org.springframework.http.HttpStatus.valueOf(errorCode.getStatus()));
    }

    // Validation 예외 처리
    @ExceptionHandler(jakarta.validation.ConstraintViolationException.class)
    protected ResponseEntity<ErrorResponse> handleConstraintVaiolationException(
            jakarta.validation.ConstraintViolationException e) {

        log.error("ConstraintViolationException: {}", e.getMessage());

        // 첫번째 에러 메시지
        String errorMessage = e.getConstraintViolations().iterator().next().getMessage();


        ErrorResponse response =
                ErrorResponse.builder().status(400).code("C001").message(errorMessage).build();


        return new ResponseEntity<>(response, org.springframework.http.HttpStatus.BAD_REQUEST);
    }

    // 다른 모든 예외 처리
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled Exception: ", e);
        ErrorResponse response = ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);

        return new ResponseEntity<>(response,
                org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
    }


}
