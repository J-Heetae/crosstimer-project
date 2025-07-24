package com.goose.crosstimer.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResult> handleCustomException(CustomException ex, HttpServletRequest request) {
        ErrorCode errorCode = ex.getErrorCode();
        log.warn("CustomException 발생 - [{} {}] code: {}, message: {}, cause: {}",
                request.getMethod(),
                request.getRequestURI(),
                errorCode.getCode(),
                ex.getMessage(),
                ex.getCause() != null ? ex.getCause().toString() : "없음",
                ex);

        return new ResponseEntity<>(
                ErrorResult.of(errorCode, ex.getMessage(), request.getRequestURI()),
                errorCode.getStatus()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResult> handleException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled Exception 발생 - [{} {}] message: {}, cause: {}",
                request.getMethod(),
                request.getRequestURI(),
                ex.getMessage(),
                ex.getCause() != null ? ex.getCause().toString() : "없음",
                ex);

        return new ResponseEntity<>(
                ErrorResult.of(ErrorCode.DB_ERROR, "서버 내부 오류가 발생했습니다.", request.getRequestURI()),
                ErrorCode.DB_ERROR.getStatus()
        );
    }
}
