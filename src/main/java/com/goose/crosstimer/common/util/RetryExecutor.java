package com.goose.crosstimer.common.util;

import com.goose.crosstimer.common.exception.CustomException;
import com.goose.crosstimer.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.function.Supplier;

@Slf4j
@Component
public class RetryExecutor {
    private static final int MAX_ATTEMPTS = 3;
    private static final long DELAY_MS = 1000;

    public <T> T executeWithRetry(Supplier<T> supplier, String description) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                return supplier.get();
            } catch (Exception e) {
                log.warn("[시도 {}] {} 실패: {}", attempt, description, e.getMessage());
                if (attempt == MAX_ATTEMPTS) {
                    log.error("[최종 실패] {}: {}", description, e.getMessage(), e);
                    throw new CustomException(ErrorCode.DB_ERROR, e);
                }
                try {
                    Thread.sleep(DELAY_MS);
                } catch (InterruptedException e1) {
                }
            }
        }
        return null;
    }

    public void runWithRetry(Runnable runnable, String description) {
        executeWithRetry(() -> {
            runnable.run();
            return null;
        }, description);
    }
}
