package com.goose.crosstimer.crossroad.dto;

public record CrossroadRangeResponse(
        Integer crossroadId,
        String name,
        Double lat,
        Double lng
) {
}
