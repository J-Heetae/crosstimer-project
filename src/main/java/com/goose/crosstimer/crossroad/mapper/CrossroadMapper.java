package com.goose.crosstimer.crossroad.mapper;

import com.goose.crosstimer.api.dto.TDataCrossroadResponse;
import com.goose.crosstimer.crossroad.domain.Crossroad;

public class CrossroadMapper {
    public static Crossroad fromDto(TDataCrossroadResponse dto) {
        return Crossroad.builder()
                .itstId(dto.itstId())
                .name(dto.name())
                .lat(dto.lat())
                .lng(dto.lng())
                .build();
    }
}
