package com.goose.crosstimer.signal.mapper;

import com.goose.crosstimer.api.dto.TDataSignalResponse;
import com.goose.crosstimer.signal.domain.SignalLog;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mapper(componentModel = "spring")
public interface SignalLogMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "itstId", source = "itstId")
    @Mapping(target = "trsmUtcTime", source = "trsmUtcTime")
    @Mapping(target = "loggedAt", expression = "java(java.time.Instant.now())")
    @Mapping(target = "signals", ignore = true)
    SignalLog toDocument(TDataSignalResponse dto);

    @AfterMapping
    default void buildSignals(TDataSignalResponse dto,
                              @MappingTarget SignalLog.SignalLogBuilder builder) {
        Map<String, SignalLog.SignalData> signals = Stream.of(
                        entry("n", dto.ntPdsgRmdrCs(), dto.ntPdsgStatNm()),
                        entry("e", dto.etPdsgRmdrCs(), dto.etPdsgStatNm()),
                        entry("s", dto.stPdsgRmdrCs(), dto.stPdsgStatNm()),
                        entry("w", dto.wtPdsgRmdrCs(), dto.wtPdsgStatNm()),
                        entry("ne", dto.nePdsgRmdrCs(), dto.nePdsgStatNm()),
                        entry("se", dto.sePdsgRmdrCs(), dto.sePdsgStatNm()),
                        entry("sw", dto.swPdsgRmdrCs(), dto.swPdsgStatNm()),
                        entry("nw", dto.nwPdsgRmdrCs(), dto.nwPdsgStatNm())
                )
                .filter(e -> e.getValue().getStatus() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        builder.signals(signals);
    }

    private static Map.Entry<String, SignalLog.SignalData> entry(String dir, Integer secs, String status) {
        return new AbstractMap.SimpleEntry<>(
                dir, new SignalLog.SignalData(secs, status)
        );
    }
}
