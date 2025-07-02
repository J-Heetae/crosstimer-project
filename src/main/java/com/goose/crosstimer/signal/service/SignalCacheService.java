package com.goose.crosstimer.signal.service;

import com.goose.crosstimer.signal.domain.SignalCache;
import com.goose.crosstimer.signal.domain.SignalCycle;
import com.goose.crosstimer.signal.domain.SignalLog;
import com.goose.crosstimer.signal.repository.SignalCacheRepository;
import com.goose.crosstimer.signal.repository.SignalCycleJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignalCacheService {
    private final SignalCacheRepository cacheRepository;
    private final SignalCycleJpaRepository cycleRepository;

    private static final Duration TTL = Duration.ofMinutes(5L);

    /**
     * 단일 교차로·방향별 SignalCache 저장
     *
     * @param itstId      교차로 ID
     * @param direction   신호 방향
     * @param signalCache 저장할 SignalCache 객체
     */
    public void saveCache(Integer itstId, String direction, SignalCache signalCache) {
        signalCache.setId(itstId + ":" + direction);
        cacheRepository.save(signalCache);
    }

    /**
     * Redis에서 교차로·방향별 SignalCache 조회
     *
     * @param itstId    교차로 ID
     * @param direction 신호 방향
     * @return 조회된 SignalCache 객체 또는 캐시가 없으면 null
     */
    public SignalCache findCache(Integer itstId, String direction) {
        String key = itstId + ":" + direction;
        return cacheRepository.findById(key).orElse(null);
    }

    /**
     * 배치 처리된 SignalLog 목록을 기반으로 최신 로그를 선택하고,
     * MySQL의 SignalCycle 정보를 이용해 예측 데이터를 추가 후 Redis에 저장합니다.
     *
     * @param logList 처리할 SignalLog 목록
     */
    public void cacheSignal(List<SignalLog> logList) {
        log.info("cacheSignal 시작: 로그 수 = {}", logList.size());

        Map<String, SignalCycle> cycleMap = getCycleMap();
        log.info("SignalCycle 맵 생성 완료: 엔트리 수 = {}", cycleMap.size());

        Map<String, List<SignalLog>> logMap = new HashMap<>();
        for (SignalLog log : logList) {
            String rawKey = rawKey(log.getItstId(), log.getDirection());
            logMap.computeIfAbsent(rawKey, k -> new ArrayList<>()).add(log);
        }

        int cacheCount = 0, skipCount = 0;
        for (Map.Entry<String, SignalCycle> entry : cycleMap.entrySet()) {
            String rawKey = entry.getKey();
            SignalCycle cycle = entry.getValue();

            List<SignalLog> signalLogs = logMap.get(rawKey);
            if (signalLogs == null || signalLogs.isEmpty()) {
//                log.info("해당 신호의 로그 없음, 스킵 rawKey={}", rawKey);
                skipCount++;
                continue;
            }
            signalLogs.sort(Comparator.comparing(SignalLog::getSignalTimestamp));
            SignalLog recentLog = signalLogs.get(signalLogs.size() - 1);

            SignalCache cache = SignalCache.builder()
                    .signalTimestamp(recentLog.getSignalTimestamp())
                    .status(recentLog.getStatus())
                    .remaining(recentLog.getRemaining())
                    .predictedGreenSec(cycle.getGreenSec())
                    .predictedRedSec(cycle.getRedSec())
                    .updatedAt(Instant.now())
                    .build();

            saveCache(recentLog.getItstId(), recentLog.getDirection().toUpperCase(), cache);
//            log.info("캐시 저장: key={}, cache={}", redisKey, cache);
            cacheCount++;
        }
        log.info("cacheSignal 완료: 저장된 캐시 수 = {}, 스킵된 항목 수 = {}, 만료 시간 = {}", cacheCount, skipCount, TTL);
    }

    /**
     * MySQL에 저장된 모든 SignalCycle을 조회하여 교차로ID:방향을 키로 사용하는 Map으로 변환
     *
     * @return 교차로ID:방향을 키로 갖는 SignalCycle Map
     */
    private Map<String, SignalCycle> getCycleMap() {
        Map<String, SignalCycle> cycleMap = new HashMap<>();
        for (SignalCycle signalCycle : cycleRepository.findAllWithCrossroad()) {
            String rawKey = rawKey(signalCycle.getCrossroad().getItstId(), signalCycle.getDirection());
            cycleMap.put(rawKey, signalCycle);
        }
        return cycleMap;
    }

    /**
     * 교차로 ID와 신호 방향으로 Map Key 생성
     *
     * @param itstId    교차로 ID
     * @param direction 신호 방향
     * @return Map Key
     */
    private String rawKey(Integer itstId, String direction) {
        return itstId + ":" + direction.toUpperCase();
    }
}
