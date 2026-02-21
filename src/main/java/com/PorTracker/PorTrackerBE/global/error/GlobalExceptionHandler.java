package com.PorTracker.PorTrackerBE.global.error;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice // 모든 Controller 에러 여기서 가로챔
public class GlobalExceptionHandler {

    // DTO @Valid처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleDtoValidationException(
            MethodArgumentNotValidException e) {
        String errorMessage =
                e.getBindingResult().getFieldErrors().stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.joining(", "));

        log.error("Validation failed: {}", errorMessage);

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

        ErrorResponse response =
                ErrorResponse.builder()
                        .status(errorCode.getStatus())
                        .code(errorCode.getCode())
                        .message(errorMessage)
                        .build();

        return new ResponseEntity<>(response, org.springframework.http.HttpStatus.BAD_REQUEST);
    }

    // JSON 파싱 validation
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonException(HttpMessageNotReadableException e) {
        String detail = "유효하지 않은 형식 및 값입니다.";

        Throwable cause = e.getCause();
        String fieldName = "확인되지 않은 필드명";
        if (cause instanceof JsonMappingException jme) {

            fieldName =
                    jme.getPath().stream()
                            .map(JsonMappingException.Reference::getFieldName)
                            .filter(Objects::nonNull)
                            .collect(Collectors.joining("."));
        }

        // Case 1: @JsonCreator에서 우리가 던진 IllegalArgumentException을 Jackson이 감쌌을 때
        if (cause instanceof ValueInstantiationException) {
            ValueInstantiationException vie = (ValueInstantiationException) cause;
            Class<?> targetType = vie.getType().getRawClass();

            String rejectedValue =
                    vie.getCause() != null ? vie.getCause().getMessage() : "알 수 없는 값";

            if (targetType != null && targetType.isEnum()) {
                // 우리가 throw new IllegalArgumentException(value) 로 던진 '틀린 값'을 꺼냄
                String possibleValues = Arrays.toString(targetType.getEnumConstants());

                detail =
                        String.format(
                                "필드 %s 값이 유효하지 않습니다: '%s'. 다음 중 하나여야 합니다: %s",
                                fieldName, rejectedValue, possibleValues);
            } else if (targetType != null && targetType.equals(OffsetDateTime.class)) {
                detail =
                        String.format(
                                "%s의 날짜 형식이 유효하지 않습니다: %s. YYYY-MM-DD 또는 ISO (예: 2026-02-21T15:30:00+09:00) 만 가능합니다.",
                                fieldName, rejectedValue);
            }
        }
        // Case 2: Jackson이 기본적으로 Enum 파싱을 실패했을 때 (혹시 모를 대비용)
        else if (cause instanceof InvalidFormatException) {
            InvalidFormatException ife = (InvalidFormatException) cause;
            Class<?> targetType = ife.getTargetType();

            if (targetType != null && targetType.isEnum()) {
                String possibleValues = Arrays.toString(targetType.getEnumConstants());
                detail =
                        String.format(
                                "필드 %s 값이 유효하지 않습니다: '%s'. 다음 중 하나여야 합니다: %s",
                                fieldName, ife.getValue(), possibleValues);
            } else if (targetType != null) {
                if (Number.class.isAssignableFrom(targetType) || targetType.isPrimitive()) {
                    detail = String.format("%s에 소수점이나 유효하지 않은 숫자 형식이 있습니다.", fieldName);
                }
            }
        }

        log.error("JSON parsing error: {}", e.getMessage());

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

        ErrorResponse response =
                ErrorResponse.builder()
                        .status(errorCode.getStatus())
                        .code(errorCode.getCode())
                        // .detail(detail)
                        .detail(detail)
                        .message(errorCode.getMessage())
                        .build();

        return new ResponseEntity<>(response, org.springframework.http.HttpStatus.BAD_REQUEST);
    }

    // 필수 파라미터 누락 시 발생하느ㅏㄴ 예외 처리
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e) {
        log.error("MissingServletRequestParameterException Occur!: {}", e.getMessage());

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

        ErrorResponse response =
                ErrorResponse.builder()
                        .status(errorCode.getStatus())
                        .code(errorCode.getCode())
                        .message(
                                String.format(
                                        "%s parameter required, but no.", e.getParameterName()))
                        .build();

        return new ResponseEntity<>(response, org.springframework.http.HttpStatus.BAD_REQUEST);
    }

    // 직접 정의한 BusinessException 에러 핸들링
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handleBusinessException(BusinessException e) {
        log.error("BusinessException: {}", e.getErrorCode().getMessage());
        ErrorCode errorCode = e.getErrorCode();
        // ErrorResponse response = ErrorResponse.of(errorCode);
        ErrorResponse response;
        String detail = e.getDetail();
        if (detail != null && !detail.isEmpty()) {
            response = ErrorResponse.of(errorCode, detail);
        } else {
            response = ErrorResponse.of(errorCode);
        }

        return new ResponseEntity<>(
                response, org.springframework.http.HttpStatus.valueOf(errorCode.getStatus()));
    }

    // 파라미터Validation 예외 처리
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

        return new ResponseEntity<>(
                response, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
