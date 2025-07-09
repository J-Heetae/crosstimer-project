package com.goose.crosstimer.signal.dto;

public record SignalCacheResponse(
        String direction,
        String status,
        Integer remainingSec
) {
}
