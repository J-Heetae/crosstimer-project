package com.goose.crosstimer.mapper;

import com.goose.crosstimer.api.dto.CrossroadResponseDto;
import com.goose.crosstimer.domain.Crossroad;

public class CrossroadMapper {
    public static Crossroad fromDto(CrossroadResponseDto dto) {
        return Crossroad.builder()
                .itstId(dto.itstId())
                .name(dto.itstNm())
                .lat(dto.mapCtptIntLat())
                .lng(dto.mapCtptIntLot())
                .build();
    }
}
