package com.goose.crosstimer.crossroad.dto;


public record CrossroadRangeRequest(
        Double swLat,
        Double swLng,
        Double neLat,
        Double neLng
) {
}
