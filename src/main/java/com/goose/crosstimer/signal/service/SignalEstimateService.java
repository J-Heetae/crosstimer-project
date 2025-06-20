package com.goose.crosstimer.signal.service;

import com.goose.crosstimer.common.util.RetryExecutor;
import com.goose.crosstimer.crossroad.domain.Crossroad;
import com.goose.crosstimer.crossroad.repository.CrossroadJpaRepository;
import com.goose.crosstimer.signal.domain.SignalCycle;
import com.goose.crosstimer.signal.domain.SignalLog;
import com.goose.crosstimer.signal.repository.SignalCycleJpaRepository;
import com.goose.crosstimer.signal.repository.SignalLogMongoRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class SignalEstimateService {
    private final SignalCycleJpaRepository cycleJpaRepository;
    private final SignalLogMongoRepository logMongoRepository;
    private final CrossroadJpaRepository crossroadJpaRepository;
    private final RetryExecutor retryExecutor;

    private static final double ALPHA = 0.3;
    private static final long MAX_INTERVAL_MS = 300_000L;

    //    @PostConstruct
    public void init() {
        upsertCycle();
    }

    public Map<Integer, Crossroad> getAllCrossroadByMap() {
        List<Crossroad> crossroadList = crossroadJpaRepository.findAll();

        log.info("CrossroadList 조회 완료 총 {}개", crossroadList.size());

        return crossroadList.stream()
                .collect(Collectors.toMap(Crossroad::getItstId, c -> c));
    }

    public Map<String, SignalCycle> getAllSignalCycleByMap() {
        List<SignalCycle> signalCycleList = cycleJpaRepository.findAllWithCrossroad();

        log.info("SignalCycleList 조회 완료 총 {}개", signalCycleList.size());

        return signalCycleList.stream()
                .collect(Collectors.toMap(c -> c.getCrossroad().getItstId() + "_" + c.getDirection().toUpperCase(Locale.ROOT), c -> c));
    }

    public void upsertCycle() {
        log.info("updateCycle 시작");

        List<SignalCycle> cycleList = new ArrayList<>();

        Map<Integer, Crossroad> crossroadMap = getAllCrossroadByMap();
        Map<String, SignalCycle> cycleMap = getAllSignalCycleByMap();

        List<SignalLog> logList = logMongoRepository.findAll();
        log.info("SignalDirectionLogList 조회 완료 총 {}개", logList.size());

        if (logList.isEmpty()) {
            log.info("SignalLog가 존재하지 않습니다.");
            return;
        }

        Map<String, List<SignalLog>> groupedLogList = logList.stream()
                .collect(Collectors.groupingBy(log -> log.getItstId() + "_" + log.getDirection().toUpperCase(Locale.ROOT)));

        for (Map.Entry<String, List<SignalLog>> entry : groupedLogList.entrySet()) {
            String entryKey = entry.getKey();
            String[] parts = entry.getKey().split("_");

            if (parts.length != 2) {
                continue;
            }

            Integer currItstId;

            try {
                currItstId = Integer.parseInt(parts[0]);
            } catch (NumberFormatException e) {
                log.warn("itstId 파싱 실패: {}", parts[0], e);
                continue;
            }

            String currDirection = parts[1].toUpperCase(Locale.ROOT);

            if (!crossroadMap.containsKey(currItstId)) {
                continue;
            }

            List<SignalLog> currLogList = entry.getValue();
            currLogList.sort(Comparator.comparingLong(SignalLog::getTrsmUtcTime));

            SignalCycle originSignalCycle = null;
            if (cycleMap.containsKey(entryKey)) {
                originSignalCycle = cycleMap.get(entryKey);
            }

            double greenMs = (originSignalCycle != null) ? originSignalCycle.getGreenSeconds() * 1000 : 0;
            double redMs = (originSignalCycle != null) ? originSignalCycle.getRedSeconds() * 1000 : 0;
            long referenceGreenStart = (originSignalCycle != null) ? originSignalCycle.getReferenceGreenStart() : 0L;

            for (int i = 1; i < currLogList.size(); i++) {
                var prev = currLogList.get(i - 1);
                var curr = currLogList.get(i);

                long prevSwitchTime = prev.getTrsmUtcTime() + prev.getRemainingDeciSeconds() * 100L;
                long currSwitchTime = curr.getTrsmUtcTime() + curr.getRemainingDeciSeconds() * 100L;

                if (currSwitchTime == prevSwitchTime || currSwitchTime - prevSwitchTime > MAX_INTERVAL_MS) {
                    continue;
                }

                if (prev.getStatus().equals("GREEN") && curr.getStatus().equals("RED")) {
                    redMs = exponentialSmoothing(redMs, (currSwitchTime - prevSwitchTime));
                    referenceGreenStart = currSwitchTime;
                }
                if (prev.getStatus().equals("RED") && curr.getStatus().equals("GREEN")) {
                    greenMs = exponentialSmoothing(greenMs, (currSwitchTime - prevSwitchTime));
                    referenceGreenStart = prevSwitchTime;
                }
            }

            if (greenMs != 0 && redMs != 0 && referenceGreenStart != 0L) {
                SignalCycle.SignalCycleBuilder builder = SignalCycle.builder();

                if (originSignalCycle != null) {
                    builder.id(originSignalCycle.getId());
                }

                cycleList.add(builder
                        .crossroad(crossroadMap.get(currItstId))
                        .direction(currDirection.toUpperCase(Locale.ROOT))
                        .referenceGreenStart(referenceGreenStart)
                        .greenSeconds((int) (greenMs / 1000.0))
                        .redSeconds((int) (redMs / 1000.0))
                        .updatedAt(Instant.now())
                        .build());
            }
        }
        retryExecutor.runWithRetry(() -> cycleJpaRepository.saveAll(cycleList), "SignalCycle 저장");
        retryExecutor.runWithRetry(() -> logMongoRepository.deleteAll(logList), "SignalLog 삭제");
    }

    private double exponentialSmoothing(double original, double next) {
        if (original == 0) {
            return next;
        }
        return ALPHA * next + (1 - ALPHA) * original;
    }
}
