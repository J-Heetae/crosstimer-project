package com.goose.crosstimer.signal.dto;

import com.goose.crosstimer.common.dto.SignalData;

import java.util.Map;

public record SignalResponse(
        Map<String, SignalData> signals
) {
}
