package com.goose.crosstimer.signal.mapper;

import com.goose.crosstimer.api.dto.TDataSignalResponse;
import com.goose.crosstimer.signal.domain.SignalCache;
import com.goose.crosstimer.signal.domain.SignalInfo;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Component
public class SignalCacheMapper {
    private static final int DEFAULT_TTL = 1;
    private static final int INVALID_DECI_SEC = 36001;
    private static final String DARK = "dark";

    public SignalCache fromTDataResponse(TDataSignalResponse response) {
        List<SignalInfo> infos = toSignalInfoList(response);
        int minSecForTTL = infos.stream()
                .mapToInt(SignalInfo::getRemainingSec)
                .min()
                .orElse(DEFAULT_TTL);

        return new SignalCache(
                response.crossroadId(),
                infos,
                Instant.ofEpochMilli(response.sendAt()),
                Instant.now(),
                minSecForTTL
        );
    }

    private List<SignalInfo> toSignalInfoList(TDataSignalResponse response) {
        return Stream.of(
                        new SignalHelper("N", response.nStatus(), response.nDeciSec()),
                        new SignalHelper("E", response.eStatus(), response.eDeciSec()),
                        new SignalHelper("S", response.sStatus(), response.sDeciSec()),
                        new SignalHelper("W", response.wStatus(), response.wDeciSec()),
                        new SignalHelper("NE", response.neStatus(), response.neDeciSec()),
                        new SignalHelper("NW", response.nwStatus(), response.nwDeciSec()),
                        new SignalHelper("SE", response.seStatus(), response.seDeciSec()),
                        new SignalHelper("SW", response.swStatus(), response.swDeciSec())
                )
                .filter(this::isValid)
                .map(this::mapToSignalInfo)
                .toList();
    }

    private boolean isValid(SignalHelper helper) {
        return Objects.nonNull(helper.status())
                && !DARK.equals(helper.status())
                && helper.remainingDeciSec() != INVALID_DECI_SEC;
    }

    private SignalInfo mapToSignalInfo(SignalHelper helper) {
        String fixedStatus = fixStatus(helper.status());
        int fixedDuration = helper.remainingDeciSec() / 10;
        return new SignalInfo(helper.direction(), fixedStatus, fixedDuration);
    }

    private static String fixStatus(String status) {
        return (status.equals("stop-And-Remain")) ? "RED" : "GREEN";
    }

    private record SignalHelper(
            String direction,
            String status,
            Integer remainingDeciSec) {
    }
}
