package com.goose.crosstimer.signal.service;

import com.goose.crosstimer.crossroad.domain.Crossroad;
import com.goose.crosstimer.crossroad.repository.CrossroadJpaRepository;
import com.goose.crosstimer.signal.domain.SignalCycle;
import com.goose.crosstimer.signal.domain.SignalLog;
import com.goose.crosstimer.signal.dto.HoltWinters;
import com.goose.crosstimer.signal.repository.SignalCycleJpaRepository;
import com.goose.crosstimer.signal.repository.SignalLogMongoRepository;
import com.goose.crosstimer.common.util.RetryExecutor;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 신호 주기 예측 모델 서비스
 * - 녹색/적색 각각에 대해 Holt–Winters 모델을 분리 관리
 * - MongoDB 로그로 모델을 업데이트하고, 결과를 RDB의 SignalCycle에 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignalPredictionService {
    private final CrossroadJpaRepository crossroadRepository;
    private final SignalLogMongoRepository logRepository;
    private final SignalCycleJpaRepository cycleRepository;
    private final RetryExecutor retryExecutor;

    // key = "itstId:direction"
    private final Map<String, HoltWinters> greenModels = new ConcurrentHashMap<>();
    private final Map<String, HoltWinters> redModels = new ConcurrentHashMap<>();

    private static final double ALPHA = 0.5;
    private static final double BETA = 0.3;
    private static final double GAMMA = 0.2;
    private static final int SEASON_LENGTH = 360;

    /**
     * 앱 구동 시: 기존 SignalCycle 엔티티 기반 단일 값으로 모델 초기화
     */
    @PostConstruct
    public void initModels() {
        List<SignalCycle> cycles = cycleRepository.findAllWithCrossroad();
        for (SignalCycle cycle : cycles) {
            String key = buildKey(cycle.getCrossroad().getItstId(), cycle.getDirection());
            greenModels.put(key, new HoltWinters(
                    new double[]{cycle.getGreenSec()}, SEASON_LENGTH, ALPHA, BETA, GAMMA));
            redModels.put(key, new HoltWinters(
                    new double[]{cycle.getRedSec()}, SEASON_LENGTH, ALPHA, BETA, GAMMA));
        }
        log.info("Initialized {} green and {} red models", greenModels.size(), redModels.size());
    }

    /**
     * MongoDB 로그에서 RED→GREEN, GREEN→RED 전환 간격을 뽑아
     * 녹/적 모델을 각각 update, 예측 결과를 SignalCycle에 저장
     * 전체 Cycle을 미리 로드하고 JPA 변경 감지를 활용
     */
    @Transactional
    public void updateAllModels(List<SignalLog> logList) {
        if (logList.isEmpty()) {
            log.info("전달된 로그 없음, 업데이트 종료");
            return;
        }
        log.info("예측 모델 업데이트 시작 ({} logs)", logList.size());

        //교차로:방향별로 로그 그룹핑
        log.info("교차로 방향별로 로그 그룹핑");
        Map<String, List<SignalLog>> grouped = new HashMap<>();
        for (SignalLog signalLog : logList) {
            String key = buildKey(signalLog.getItstId(), signalLog.getDirection());
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(signalLog);
        }
        log.info("생성된 grouped size = {}", grouped.size());

        //기존 Crossroad 맵 생성
        log.info("기존 Crossroad 맵 생성");
        Map<String, Crossroad> crossroadMap = new HashMap<>();
        for (Crossroad crossroad : crossroadRepository.findAll()) {
            crossroadMap.put(crossroad.getItstId().toString(), crossroad);
        }

        //모든 SignalCycle 엔티티 미리 로드 및 맵으로 저장
        List<SignalCycle> cycles = cycleRepository.findAllWithCrossroad();
        log.info("모든 SignalCycle 엔티티 미리 로드 및 맵으로 저장, 엔티티 개수 = {}", cycles.size());

        Map<String, SignalCycle> cycleMap = new HashMap<>();
        for (SignalCycle cycle : cycles) {
            String key = buildKey(cycle.getCrossroad().getItstId(), cycle.getDirection());
            cycleMap.put(key, cycle);
        }

        //각 키별로 모델 업데이트 및 엔티티 필드 수정
        log.info("각 키별로 모델 업데이트 및 엔티티 필드 수정");
        for (var entry : grouped.entrySet()) {
            String key = entry.getKey();
            String[] parts = key.split(":");
            int itstId = Integer.parseInt(parts[0]);
            String direction = parts[1];

            List<SignalLog> logs = entry.getValue();
            logs.sort(Comparator.comparing(SignalLog::getSignalTimestamp));

            List<Double> greenSec = new ArrayList<>();
            List<Double> redSec = new ArrayList<>();
            for (int i = 1; i < logs.size(); i++) {
                SignalLog prev = logs.get(i - 1);
                SignalLog curr = logs.get(i);

                long prevChangeMs = prev.getSignalTimestamp().toEpochMilli() + prev.getRemaining() * 1000;
                long currChangeMs = curr.getSignalTimestamp().toEpochMilli() + curr.getRemaining() * 1000;
                double dtSec = (currChangeMs - prevChangeMs) / 1000.0;

                if ("GREEN".equals(prev.getStatus()) && "RED".equals(curr.getStatus())) {
                    redSec.add(dtSec);
                } else if ("RED".equals(prev.getStatus()) && "GREEN".equals(curr.getStatus())) {
                    greenSec.add(dtSec);
                }
            }
            if (greenSec.isEmpty() || redSec.isEmpty()) continue;

//            log.info("이상치 제거 후 필터링된 데이터 사용");
            List<Double> filteredGreen = removeOutliers(greenSec);
            List<Double> filteredRed = removeOutliers(redSec);
            if (filteredGreen.isEmpty() || filteredRed.isEmpty()) continue;

            //모델 학습/예측
//            log.info("모델 학습/예측");
            HoltWinters greenModel = greenModels.computeIfAbsent(key, k ->
                    new HoltWinters(toArray(filteredGreen), SEASON_LENGTH, ALPHA, BETA, GAMMA));
            greenModel.update(toArray(filteredGreen));
            HoltWinters redModel = redModels.computeIfAbsent(key, k ->
                    new HoltWinters(toArray(filteredRed), SEASON_LENGTH, ALPHA, BETA, GAMMA));
            redModel.update(toArray(filteredRed));

            int nextGreen;
            int nextRed;
            // 데이터 포인트 부족 시 중앙값 반환
            if (greenModel.getDataLength() < 2) {
                nextGreen = (int) Math.round(filteredGreen.stream().mapToDouble(d -> d).average().orElse(0));
            } else {
                nextGreen = (int) Math.round(greenModel.forecast(1)[0]);
            }
            if (redModel.getDataLength() < 2) {
                nextRed = (int) Math.round(filteredRed.stream().mapToDouble(d -> d).average().orElse(0));
            } else {
                nextRed = (int) Math.round(redModel.forecast(1)[0]);
            }

            SignalCycle cycle;
            if (!cycleMap.containsKey(key)) {
                Crossroad crossroad = crossroadMap.get(String.valueOf(itstId));
                if (crossroad == null) {
//                    log.warn("Crossroad 정보 없음, 건너뜀: {}", key);
                    continue;
                }
                cycle = SignalCycle.builder()
                        .crossroad(crossroad)
                        .direction(direction)
                        .greenSec(0)
                        .redSec(0)
                        .build();
                cycles.add(cycle);
//                log.info("신규 SignalCycle 생성: {}", key);
            } else {
                cycle = cycleMap.get(key);
//                log.info("기존 SignalCycle: {}", cycle.toString());
            }

            //엔티티 필드 수정 (JPA 변경 감지)
            cycle.applyPrediction(nextGreen, nextRed, Instant.now());
        }

        //DB 저장: 변경 감지된 엔티티를 한 번에 flush
        retryExecutor.runWithRetry(() -> cycleRepository.saveAll(cycles), "SignalCycle 저장");
        log.info("업데이트된 {}개의 SignalCycle 저장 완료", cycles.size());
        //MongoDB 로그 삭제
//        retryExecutor.runWithRetry(logRepo::deleteAll, "SignalLog 삭제");
    }

    private List<Double> removeOutliers(List<Double> list) {
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        // 1) 중앙값 계산
        List<Double> sorted = new ArrayList<>(list);
        Collections.sort(sorted);
        double median;
        int n = sorted.size();
        if (n % 2 == 0) {
            median = (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
        } else {
            median = sorted.get(n / 2);
        }
        // 2) ±30% 범위 필터링
        double lower = median * 0.7;
        double upper = median * 1.3;
        return list.stream()
                .filter(d -> d >= lower && d <= upper)
                .collect(Collectors.toList());
    }

    private static double[] toArray(List<Double> list) {
        double[] arr = new double[list.size()];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }

    private String buildKey(int itstId, String direction) {
        return itstId + ":" + direction.toUpperCase();
    }
}
