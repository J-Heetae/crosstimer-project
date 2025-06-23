package com.goose.crosstimer.signal.service;

import com.goose.crosstimer.signal.domain.SignalCache;
import com.goose.crosstimer.signal.domain.SignalCycle;
import com.goose.crosstimer.signal.domain.SignalLog;
import com.goose.crosstimer.signal.repository.SignalCycleJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignalCacheService {
    private final RedisTemplate<String, SignalCache> redisTemplate;
    private final SignalCycleJpaRepository cycleRepository;

    private static final Duration TTL = Duration.ofMinutes(5L);

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

            String redisKey = buildKey(recentLog.getItstId(), recentLog.getDirection().toUpperCase());
            redisTemplate.opsForValue().set(redisKey, cache);
//            log.info("캐시 저장: key={}, cache={}", redisKey, cache);
            cacheCount++;
        }
        log.info("cacheSignal 완료: 저장된 캐시 수 = {}, 스킵된 항목 수 = {}, 만료 시간 = {}", cacheCount, skipCount, TTL);
    }

    private String buildKey(Integer itstId, String direction) {
        return "signal:" + itstId + ":" + direction.toUpperCase();
    }

    private String rawKey(Integer itstId, String direction) {
        return itstId + ":" + direction.toUpperCase();
    }

    private Map<String, SignalCycle> getCycleMap() {
        Map<String, SignalCycle> cycleMap = new HashMap<>();
        for (SignalCycle signalCycle : cycleRepository.findAllWithCrossroad()) {
            String rawKey = rawKey(signalCycle.getCrossroad().getItstId(), signalCycle.getDirection());
            cycleMap.put(rawKey, signalCycle);
        }
        return cycleMap;
    }
}
