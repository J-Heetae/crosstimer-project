package com.goose.crosstimer.signal.service;

import com.goose.crosstimer.api.dto.TDataSignalResponse;
import com.goose.crosstimer.api.service.TDataApiService;
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

    private final TDataApiService tDataApiService;
    private final SignalLogService signalLogService;
    private final RetryExecutor retryExecutor;
    private final SignalCacheService cacheService;
    private final SignalPredictionService predictionService;

    /**
     * 매일 9시, 12시, 15시, 18시, 21시에 신호 로그 배치를 실행합니다.
     * <p>
     * 배치 처리 순서:
     * 1. callSignalApi()로 API에서 신호 데이터 수집
     * 2. 수집된 데이터를 MongoDB에 저장 (RetryExecutor 사용)
     * 3. 예측 모델 전체 업데이트
     * 4. Redis 캐시 생성 및 저장
     */
//    @Scheduled(cron = "0 0 9,12,15,18,21 * * *", zone = "Asia/Seoul")
    @Scheduled(fixedRate = 500_000_000L)
    public void batchProcess() {
        List<SignalLog> responseList = callSignalApi();
        retryExecutor.runWithRetry(() -> signalLogService.saveLogs(responseList), "SignalLog MongoDB에 저장");
        predictionService.updateAllModels(responseList);
        cacheService.cacheSignal(responseList);
    }

    /**
     * 외부 T Data API를 최대 MAX_CALLS회 호출하여 신호 데이터를 조회하고,
     * 교차로·방향별 SignalLog 객체를 생성하여 반환합니다.
     * <p>
     * 데이터 수집 로직:
     * 1. getSignalsMaxRow(call) 호출
     * 2. 응답이 비어있으면 반복 중단
     * 3. 방향별 데이터를 SignalData로 매핑 후 유효성 검사
     * 4. SignalLog 엔티티로 변환
     * 5. 다음 호출 전 THREAD_TIME_WAIT_MILLIS 대기
     *
     * @return 생성된 SignalLog 리스트
     */
    private List<SignalLog> callSignalApi() {
        log.info("신호 잔여시간 정보 API 호출 시작");
        List<SignalLog> logList = new ArrayList<>();
        for (int call = 1; call <= MAX_CALLS; call++) {
            //신호 잔여시간 정보 API 호출
            List<TDataSignalResponse> responseList =
                    tDataApiService.getSignalsMaxRow(call);

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
                            .signalTimestamp(Instant.ofEpochMilli(response.sentAt()))
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

    /**
     * SignalData가 null, "dark" 상태, 특수 잔여시간(36001) 여부 등을 검사하여
     * 유효한 신호 데이터인지 판단합니다.
     *
     * @param signalData 검사할 SignalData 객체
     * @return 유효하면 true, 아니면 false
     */
    private boolean validateSignalData(SignalData signalData) {
        return signalData.status() != null &&
                signalData.remainingDeciSeconds() != null &&
                !signalData.status().equals("dark") &&
                !signalData.status().equals("null") &&
                signalData.remainingDeciSeconds() != 36001;
    }

    /**
     * 신호 상태를 "RED" 또는 "GREEN"으로 반환
     *
     * @param signalData 상태를 변경할 SignalData
     * @return "stop-And-Remain"인 경우 "RED", 그 외에는 "GREEN"
     */
    private String getFixedStatus(SignalData signalData) {
        return signalData.status().equals("stop-And-Remain") ? "RED" : "GREEN";
    }

    /**
     * TDataSignalResponse에서 방향별 신호 정보를 추출하여
     * 방향 코드를 키로 사용하는 Map으로 반환합니다.
     *
     * @param response 외부 API 응답 객체
     * @return 방향별 키와 SignalData 값의 Map
     */
    private static Map<String, SignalData> getStringSignalDataMap(TDataSignalResponse response) {
        Map<String, SignalData> directionMap = new HashMap<>();
        directionMap.put("N", new SignalData(response.nSec(), response.nStatus()));
        directionMap.put("E", new SignalData(response.eSec(), response.eStatus()));
        directionMap.put("S", new SignalData(response.sSec(), response.sStatus()));
        directionMap.put("W", new SignalData(response.wSec(), response.wStatus()));
        directionMap.put("NE", new SignalData(response.neSec(), response.neStatus()));
        directionMap.put("NW", new SignalData(response.nwSec(), response.nwStatus()));
        directionMap.put("SE", new SignalData(response.seSec(), response.seStatus()));
        directionMap.put("SW", new SignalData(response.swSec(), response.swStatus()));
        return directionMap;
    }
}
