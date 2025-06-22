package com.goose.crosstimer.api.service;

import com.goose.crosstimer.api.client.TDataApiClient;
import com.goose.crosstimer.api.dto.TDataSignalResponse;
import com.goose.crosstimer.api.dto.TDataRequest;
import com.goose.crosstimer.common.util.RetryExecutor;
import com.goose.crosstimer.signal.dto.SignalData;
import com.goose.crosstimer.common.exception.CustomException;
import com.goose.crosstimer.signal.domain.SignalLog;
import com.goose.crosstimer.signal.service.SignalLogService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

import static com.goose.crosstimer.common.exception.ErrorCode.BATCH_LOG_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class TDataBatchService {
    private static final int MAX_CALLS = 1000;
    private static final int THREAD_TIME_WAIT_MILLIS = 1000;

    private final TDataApiClient client;
    private final SignalLogService signalLogService;
    private final RetryExecutor retryExecutor;


    @PostConstruct
    public void getSignalLog() {
        List<SignalLog> saveLogList = new ArrayList<>();
        try {
            for (int call = 1; call <= MAX_CALLS; call++) {
                //신호 잔여시간 정보 API 호출
                List<TDataSignalResponse> responseList =
                        client.getSignalInfo(TDataRequest.fromPagination(call, 1000));

                for (TDataSignalResponse response : responseList) {
                    //교차로의 방향별로 신호 구분
                    Map<String, SignalData> directionMap = getStringSignalDataMap(response);

                    for (String direction : directionMap.keySet()) {
                        SignalData currDirection = directionMap.get(direction);

                        if (!validateSignalData((currDirection))) {
                            continue;
                        }

                        saveLogList.add(SignalLog.builder()
                                .itstId(response.itstId())
                                .direction(direction)
                                .trsmUtcTime(response.trsmUtcTime())
                                .loggedAt(Instant.now())
                                .remainingDeciSeconds(currDirection.remainingDeciSeconds())
                                .status(getFixedStatus(currDirection))
                                .build());
                    }
                }
                Thread.sleep(THREAD_TIME_WAIT_MILLIS);
            }
        } catch (Exception e) {
            throw new CustomException(BATCH_LOG_ERROR, e);
        } finally {
            retryExecutor.runWithRetry(() -> signalLogService.saveLogs(saveLogList), "SignalLog 저장");
            log.info("SignalLog 총 {}개 저장 완료", saveLogList.size());
        }
    }

    private boolean validateSignalData(SignalData signalData) {
        return signalData.status() != null &&
                signalData.remainingDeciSeconds() != null &&
                !signalData.status().equals("dark") &&
                !signalData.status().equals("null") &&
                signalData.remainingDeciSeconds() != 36001;
    }

    private String getFixedStatus(SignalData signalData) {
        return signalData.status().equals("stop-And-Remain") ? "RED" : "GREEN";
    }

    private static Map<String, SignalData> getStringSignalDataMap(TDataSignalResponse response) {
        Map<String, SignalData> directionMap = new HashMap<>();
        directionMap.put("N", new SignalData(response.ntPdsgRmdrCs(), response.ntPdsgStatNm()));
        directionMap.put("E", new SignalData(response.etPdsgRmdrCs(), response.etPdsgStatNm()));
        directionMap.put("S", new SignalData(response.stPdsgRmdrCs(), response.stPdsgStatNm()));
        directionMap.put("W", new SignalData(response.wtPdsgRmdrCs(), response.wtPdsgStatNm()));
        directionMap.put("NE", new SignalData(response.nePdsgRmdrCs(), response.nePdsgStatNm()));
        directionMap.put("NW", new SignalData(response.nwPdsgRmdrCs(), response.nwPdsgStatNm()));
        directionMap.put("SE", new SignalData(response.sePdsgRmdrCs(), response.sePdsgStatNm()));
        directionMap.put("SW", new SignalData(response.swPdsgRmdrCs(), response.swPdsgStatNm()));
        return directionMap;
    }
}
