package com.goose.crosstimer.signal.dto;

import com.goose.crosstimer.common.dto.SignalData;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;
import java.util.Map;

@Getter
@AllArgsConstructor
public class SignalCache {
    private final Map<String, SignalData> signals;
    private final Instant cachedAt;
}
