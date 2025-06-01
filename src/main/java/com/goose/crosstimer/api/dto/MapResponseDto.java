package com.goose.crosstimer.api.dto;

import java.time.OffsetDateTime;

public record MapResponseDto(
        Integer itstId,           // 교차로 ID
        String itstNm,            // 교차로 한글명
        String itstEngNm,         // 교차로 영문명
        Double mapCtptIntLat,     // 중심점 위도
        Double mapCtptIntLot,     // 중심점 경도
        Double laneWidth,         // 차로폭
        String limitSpedTypeNm,   // 제한속도유형명
        String limitSped,        // 제한속도
        String rgtrId,            // 등록자 ID
        OffsetDateTime regDt      // 등록일시
) {
}