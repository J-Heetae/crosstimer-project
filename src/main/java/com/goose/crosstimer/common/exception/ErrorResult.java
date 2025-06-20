package com.goose.crosstimer.common.exception;

import java.time.LocalDateTime;

public record ErrorResult(
        int status,
        String code,
        String error,
        String message,
        String path,
        LocalDateTime timestamp
) {
    public static ErrorResult of(ErrorCode code, String message, String path) {
        return new ErrorResult(
                code.getStatus().value(),
                code.getCode(),
                code.getStatus().name(),
                message,
                path,
                LocalDateTime.now()
        );
    }
}
