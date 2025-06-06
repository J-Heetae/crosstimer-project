package com.goose.crosstimer.signal.dto;

public record DirectionalSignal(
        String direction,
        Integer remainTime,
        String status
) {
}
