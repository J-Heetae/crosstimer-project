package com.goose.crosstimer.common.util;

import com.goose.crosstimer.api.dto.TDataSignalResponse;
import com.goose.crosstimer.common.dto.SignalData;
import com.goose.crosstimer.signal.domain.SignalInfo;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class SignalMappingUtil {
    public static Map<String, SignalData> toSignalMap(TDataSignalResponse dto) {
        return Stream.of(
                        entry("n", dto.ntPdsgRmdrCs(), dto.ntPdsgStatNm()),
                        entry("e", dto.etPdsgRmdrCs(), dto.etPdsgStatNm()),
                        entry("s", dto.stPdsgRmdrCs(), dto.stPdsgStatNm()),
                        entry("w", dto.wtPdsgRmdrCs(), dto.wtPdsgStatNm()),
                        entry("ne", dto.nePdsgRmdrCs(), dto.nePdsgStatNm()),
                        entry("se", dto.sePdsgRmdrCs(), dto.sePdsgStatNm()),
                        entry("sw", dto.swPdsgRmdrCs(), dto.swPdsgStatNm()),
                        entry("nw", dto.nwPdsgRmdrCs(), dto.nwPdsgStatNm())
                )
                .filter(e -> e.getValue().status() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Map<String, SignalData> toSignalMap(SignalInfo signalInfo) {
        return Stream.of(
                        entry("n", signalInfo.getNtPdsgRmdrCs(), signalInfo.getNtPdsgStatNm()),
                        entry("e", signalInfo.getEtPdsgRmdrCs(), signalInfo.getEtPdsgStatNm()),
                        entry("s", signalInfo.getStPdsgRmdrCs(), signalInfo.getStPdsgStatNm()),
                        entry("w", signalInfo.getWtPdsgRmdrCs(), signalInfo.getWtPdsgStatNm()),
                        entry("ne", signalInfo.getNePdsgRmdrCs(), signalInfo.getNePdsgStatNm()),
                        entry("se", signalInfo.getSePdsgRmdrCs(), signalInfo.getSePdsgStatNm()),
                        entry("sw", signalInfo.getSwPdsgRmdrCs(), signalInfo.getSwPdsgStatNm()),
                        entry("nw", signalInfo.getNwPdsgRmdrCs(), signalInfo.getNwPdsgStatNm())
                )
                .filter(e -> e.getValue().status() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private static Map.Entry<String, SignalData> entry(
            String dir, Integer remainingDeciSeconds, String status) {
        return new AbstractMap.SimpleEntry<>(
                dir, new SignalData(remainingDeciSeconds, status)
        );
    }
}
