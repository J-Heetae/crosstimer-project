package com.goose.crosstimer.signal.dto;

public record SignalCycleResponse(
        String direction,
        Long referenceGreenStart,
        Integer greenSeconds,
        Integer redSeconds,
        Long updatedAt
) {
}
