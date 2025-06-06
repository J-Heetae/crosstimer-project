package com.goose.crosstimer.crossroad.dto;


public record CrossroadRangeRequest(
        Double swLat,
        Double swLot,
        Double neLat,
        Double neLot
) {
}
