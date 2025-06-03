package com.goose.crosstimer.mapper;

import com.goose.crosstimer.api.dto.CrossroadResponseDto;
import com.goose.crosstimer.domain.Crossroad;

public class CrossroadMapper {
    public static Crossroad fromDto(CrossroadResponseDto dto) {
        return Crossroad.create(dto.itstId(), dto.itstNm(), dto.mapCtptIntLat(), dto.mapCtptIntLot());
    }
}
