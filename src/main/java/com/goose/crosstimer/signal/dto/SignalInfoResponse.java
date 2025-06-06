package com.goose.crosstimer.signal.dto;

import com.goose.crosstimer.signal.domain.SignalInfo;

import java.util.ArrayList;
import java.util.List;

public record SignalInfoResponse(
        Integer itstId,
        Long trsmUtcTime,
        List<DirectionalSignal> signals
) {
    public static SignalInfoResponse from(SignalInfo signalInfo) {
        List<DirectionalSignal> signals = new ArrayList<>();

        if (signalInfo.getNtPdsgRmdrCs() != null)
            signals.add(new DirectionalSignal("NT", signalInfo.getNtPdsgRmdrCs(), signalInfo.getNtPdsgStatNm()));

        if (signalInfo.getEtPdsgRmdrCs() != null)
            signals.add(new DirectionalSignal("ET", signalInfo.getEtPdsgRmdrCs(), signalInfo.getEtPdsgStatNm()));

        if (signalInfo.getStPdsgRmdrCs() != null)
            signals.add(new DirectionalSignal("ST", signalInfo.getStPdsgRmdrCs(), signalInfo.getStPdsgStatNm()));

        if (signalInfo.getWtPdsgRmdrCs() != null)
            signals.add(new DirectionalSignal("WT", signalInfo.getWtPdsgRmdrCs(), signalInfo.getWtPdsgStatNm()));

        if (signalInfo.getNePdsgRmdrCs() != null)
            signals.add(new DirectionalSignal("NE", signalInfo.getNePdsgRmdrCs(), signalInfo.getNePdsgStatNm()));

        if (signalInfo.getSePdsgRmdrCs() != null)
            signals.add(new DirectionalSignal("SE", signalInfo.getSePdsgRmdrCs(), signalInfo.getSePdsgStatNm()));

        if (signalInfo.getSwPdsgRmdrCs() != null)
            signals.add(new DirectionalSignal("SW", signalInfo.getSwPdsgRmdrCs(), signalInfo.getSwPdsgStatNm()));

        if (signalInfo.getNwPdsgRmdrCs() != null)
            signals.add(new DirectionalSignal("NW", signalInfo.getNwPdsgRmdrCs(), signalInfo.getNwPdsgStatNm()));

        return new SignalInfoResponse(
                signalInfo.getItstId(),
                signalInfo.getTrsmUtcTime(),
                signals
        );
    }
}
