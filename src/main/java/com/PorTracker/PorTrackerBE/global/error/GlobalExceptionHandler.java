package com.PorTracker.PorTrackerBE.global.error;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

import org.springframework.web.bind.MethodArgumentNotValidException;


@Slf4j
@RestControllerAdvice // 모든 Controller 에러 여기서 가로챔
public class GlobalExceptionHandler {
    
    // DTO @Valid처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleDtoValidationException(MethodArgumentNotValidException e){
        String errorMessage = e.getBindingResult().getFieldErrors().stream().map(error->error.getField()+": "+error.getDefaultMessage()).collect(Collectors.joining(", "));

        log.error("Validation failed: {}", errorMessage);
        

        ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

        ErrorResponse response =
                ErrorResponse.builder()
                        .status(errorCode.getStatus())
                        .code(errorCode.getCode())
                        .message(
                                errorMessage)
                        .build();

        return new ResponseEntity<>(response, org.springframework.http.HttpStatus.BAD_REQUEST);
        
    }

    // JSON 파싱 validation
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleJsonException(HttpMessageNotReadableException e){
        String detail = "유효하지 않은 형식 및 값입니다.";

        // Enum에러일 경우 구체화
        if(e.getCause() instanceof InvalidFormatException){
            InvalidFormatException ife = (InvalidFormatException) e.getCause();
            if(ife.getTargetType().isEnum()){
                detail = String.format("유효하지 않은 값입니다: %s, 다음 중 하나여야 합니다: %s", ife.getValue(), Arrays.toString(ife.getTargetType().getEnumConstants()));
            }
        }

        log.error("JSON parsing error: {}", e.getMessage());
        
                ErrorCode errorCode = ErrorCode.INVALID_INPUT_VALUE;

        ErrorResponse response =
                ErrorResponse.builder()
                        .status(errorCode.getStatus())
                        .code(errorCode.getCode())
                        .detail(
                                detail)
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
