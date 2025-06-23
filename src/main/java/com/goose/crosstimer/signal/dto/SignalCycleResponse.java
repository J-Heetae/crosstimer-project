package com.goose.crosstimer.signal.dto;

public record SignalCycleResponse(
        String direction,
        Integer greenSec,
        Integer redSec,
        Long updatedAt
) {
}
