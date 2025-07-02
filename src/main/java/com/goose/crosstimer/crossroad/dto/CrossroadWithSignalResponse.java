package com.goose.crosstimer.crossroad.dto;

import com.goose.crosstimer.signal.dto.SignalCacheResponse;

import java.util.List;

public record CrossroadWithSignalResponse(
        Integer itstId,
        String name,
        Double lat,
        Double lng,
        List<SignalCacheResponse> signalCacheList
) {
}
