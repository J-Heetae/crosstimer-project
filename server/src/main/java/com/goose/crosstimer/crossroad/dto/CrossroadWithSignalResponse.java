package com.goose.crosstimer.crossroad.dto;

import com.goose.crosstimer.signal.dto.SignalCacheResponse;

import java.time.Instant;
import java.util.List;

public record CrossroadWithSignalResponse(
        Integer crossroadId,
        String name,
        Double lat,
        Double lng,
        Instant sendAt,
        Instant cachedAt,
        List<SignalCacheResponse> signalCaches
) {
}
