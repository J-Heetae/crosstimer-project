package com.goose.crosstimer.api.dto;

import java.time.OffsetDateTime;

public record SignalResponseDto(
        String dataId,          // 데이터 ID
        Integer itstId,          // 교차로 ID
        String eqmnId,          // 장비 ID

        Integer trsmYear,        // 패킷전송년도
        Integer trsmMt,          // 패킷전송월
        Integer trsmDy,         // 패킷전송일
        Integer trsmHr,         // 패킷전송시간
        Integer trsmTm,          // 패킷전송시:분:초
        Integer trsmMs,          // 패킷전송밀리초
        Long trsmUtcTime,       // 전송UTC시간

        Integer msgCreatMin,    // 메시지 생성 시간
        Integer msgCreatDs,     // 메시지 생성 분

        String rgtrId,          // 등록자 ID
        OffsetDateTime regDt,   // 등록일시

        // 보행신호 (잔여 초 / 상태명)
        Integer ntPdsgRmdrCs, String ntPdsgStatNm,
        Integer etPdsgRmdrCs, String etPdsgStatNm,
        Integer stPdsgRmdrCs, String stPdsgStatNm,
        Integer wtPdsgRmdrCs, String wtPdsgStatNm,
        Integer nePdsgRmdrCs, String nePdsgStatNm,
        Integer sePdsgRmdrCs, String sePdsgStatNm,
        Integer swPdsgRmdrCs, String swPdsgStatNm,
        Integer nwPdsgRmdrCs, String nwPdsgStatNm

) {
}