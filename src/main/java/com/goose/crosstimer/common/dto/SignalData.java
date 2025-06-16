package com.goose.crosstimer.common.dto;

/**
 * 방향별 신호 정보 (잔여 초 + 상태)
 */
public record SignalData(
        Integer remainingDeciSeconds,
        String status
) {
}
