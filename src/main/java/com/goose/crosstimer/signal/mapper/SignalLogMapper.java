package com.goose.crosstimer.signal.mapper;

import com.goose.crosstimer.api.dto.TDataSignalResponse;
import com.goose.crosstimer.common.util.SignalMappingUtil;
import com.goose.crosstimer.signal.domain.SignalLog;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

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
        builder.signals(SignalMappingUtil.toSignalMap(dto));
    }

}
