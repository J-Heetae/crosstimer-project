package com.goose.crosstimer.domain;


public record CrossroadRangeRequestDto(
        Double swLat,
        Double swLot,
        Double neLat,
        Double neLot
) {
}
