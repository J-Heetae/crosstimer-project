package com.goose.crosstimer.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TDataCrossroadResponse(
        @JsonProperty("itstId") Integer crossroadId,     // 교차로 ID
        @JsonProperty("itstNm") String name,          // 한글명
        @JsonProperty("mapCtptIntLat") Double lat,            // 위도
        @JsonProperty("mapCtptIntLot") Double lng   // 경도
) {
}