package com.goose.crosstimer.signal.mapper;

import com.goose.crosstimer.api.dto.TDataSignalResponse;
import com.goose.crosstimer.signal.domain.SignalInfo;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public class SignalInfoMapper {
    public static SignalInfo fromDto(TDataSignalResponse dto) {
        return SignalInfo.builder()
                .itstId(dto.itstId())
                .trsmUtcTime(dto.trsmUtcTime())
                .ntPdsgRmdrCs(dto.ntPdsgRmdrCs())
                .ntPdsgStatNm(dto.ntPdsgStatNm())
                .etPdsgRmdrCs(dto.etPdsgRmdrCs())
                .etPdsgStatNm(dto.etPdsgStatNm())
                .stPdsgRmdrCs(dto.stPdsgRmdrCs())
                .stPdsgStatNm(dto.stPdsgStatNm())
                .wtPdsgRmdrCs(dto.wtPdsgRmdrCs())
                .wtPdsgStatNm(dto.wtPdsgStatNm())
                .nePdsgRmdrCs(dto.nePdsgRmdrCs())
                .nePdsgStatNm(dto.nePdsgStatNm())
                .sePdsgRmdrCs(dto.sePdsgRmdrCs())
                .sePdsgStatNm(dto.sePdsgStatNm())
                .swPdsgRmdrCs(dto.swPdsgRmdrCs())
                .swPdsgStatNm(dto.swPdsgStatNm())
                .nwPdsgRmdrCs(dto.nwPdsgRmdrCs())
                .nwPdsgStatNm(dto.nwPdsgStatNm())
                .build();
    }
}
