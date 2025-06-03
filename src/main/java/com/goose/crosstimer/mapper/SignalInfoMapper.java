package com.goose.crosstimer.mapper;

import com.goose.crosstimer.api.dto.CrossroadResponseDto;
import com.goose.crosstimer.api.dto.SignalResponseDto;
import com.goose.crosstimer.domain.Crossroad;
import com.goose.crosstimer.domain.SignalInfo;

public class SignalInfoMapper {
    public static SignalInfo fromDto(SignalResponseDto dto) {
        return SignalInfo.create(
                dto.itstId(),
                dto.trsmUtcTime(),
                dto.ntPdsgRmdrCs(), dto.ntPdsgStatNm(),
                dto.etPdsgRmdrCs(), dto.etPdsgStatNm(),
                dto.stPdsgRmdrCs(), dto.stPdsgStatNm(),
                dto.wtPdsgRmdrCs(), dto.wtPdsgStatNm(),
                dto.nePdsgRmdrCs(), dto.nePdsgStatNm(),
                dto.sePdsgRmdrCs(), dto.sePdsgStatNm(),
                dto.swPdsgRmdrCs(), dto.swPdsgStatNm(),
                dto.nwPdsgRmdrCs(), dto.nwPdsgStatNm()
        );
    }
}
