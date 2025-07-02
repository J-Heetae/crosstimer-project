package com.goose.crosstimer.signal.service;

import com.goose.crosstimer.api.client.TDataApiClient;
import com.goose.crosstimer.api.dto.TDataRequest;
import com.goose.crosstimer.api.dto.TDataSignalResponse;
import com.goose.crosstimer.common.util.RetryExecutor;
import com.goose.crosstimer.signal.domain.SignalLog;
import com.goose.crosstimer.signal.dto.SignalData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignalLogBatchService {

    private static final int MAX_CALLS = 100;
    private static final int THREAD_TIME_WAIT_MILLIS = 3000;

    private final TDataApiClient client;
    private final SignalLogService signalLogService;
    private final RetryExecutor retryExecutor;
    private final SignalCacheService cacheService;
    private final SignalPredictionService predictionService;

    @Scheduled(cron = "0 0 9,12,15,18,21 * * *", zone = "Asia/Seoul")
//    @Scheduled(fixedRate = 500_000_000L)
    public void batchProcess() {
        List<SignalLog> responseList = callSignalApi();
        retryExecutor.runWithRetry(() -> signalLogService.saveLogs(responseList), "SignalLog MongoDB에 저장");
        predictionService.updateAllModels(responseList);
        cacheService.cacheSignal(responseList);
    }

    private List<SignalLog> callSignalApi() {
        log.info("신호 잔여시간 정보 API 호출 시작");
        List<SignalLog> logList = new ArrayList<>();
        for (int call = 1; call <= MAX_CALLS; call++) {
            //신호 잔여시간 정보 API 호출
            List<TDataSignalResponse> responseList =
                    client.getSignalInfo(TDataRequest.fromPagination(call, 1000));

            if (responseList.isEmpty()) {
                break;
            }
            log.info("신호 잔여시간 정보 API 호출 성공 {}번째 : row = {}개", call, responseList.size());

            for (TDataSignalResponse response : responseList) {
                //교차로의 방향별로 신호 구분
                Map<String, SignalData> directionMap = getStringSignalDataMap(response);

                for (String direction : directionMap.keySet()) {
                    SignalData currDirection = directionMap.get(direction);

                    if (!validateSignalData((currDirection))) {
                        continue;
                    }

                    logList.add(SignalLog.builder()
                            .itstId(response.itstId())
                            .direction(direction)
                            .signalTimestamp(Instant.ofEpochMilli(response.trsmUtcTime()))
                            .loggedAt(Instant.now())
                            .remaining(currDirection.remainingDeciSeconds() / 10)
                            .status(getFixedStatus(currDirection))
                            .build());
                }
            }
//            log.info("SignalLog 변환 : 현재 {}개", logList.size());
            try {
                Thread.sleep(THREAD_TIME_WAIT_MILLIS);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }
        }
        log.info("SignalLog 변환 완료 : 총 {}개", logList.size());
        return logList;
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
