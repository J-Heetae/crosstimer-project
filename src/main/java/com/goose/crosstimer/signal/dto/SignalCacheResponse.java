package com.goose.crosstimer.signal.dto;

import java.time.Instant;

public record SignalCacheResponse(
        Instant signalTimestamp,
        String direction,
        String status,
        Integer remaining,
        Integer predictedGreenSec,
        Integer predictedRedSec
) {
}
