package com.goose.crosstimer.crossroad.service;

import com.goose.crosstimer.api.dto.TDataSignalResponse;
import com.goose.crosstimer.api.service.TDataApiService;
import com.goose.crosstimer.common.exception.CustomException;
import com.goose.crosstimer.common.exception.ErrorCode;
import com.goose.crosstimer.crossroad.domain.Crossroad;
import com.goose.crosstimer.crossroad.dto.CrossroadRangeRequest;
import com.goose.crosstimer.crossroad.dto.CrossroadRangeResponse;
import com.goose.crosstimer.signal.domain.SignalCache;
import com.goose.crosstimer.crossroad.dto.CrossroadWithSignalResponse;
import com.goose.crosstimer.crossroad.repository.CrossroadJpaRepository;
import com.goose.crosstimer.signal.domain.SignalInfo;
import com.goose.crosstimer.signal.dto.SignalCacheResponse;
import com.goose.crosstimer.signal.mapper.SignalCacheMapper;
import com.goose.crosstimer.signal.repository.SignalCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrossroadService {
    private final CrossroadJpaRepository crossroadJpaRepository;
    private final SignalCacheRepository signalCacheRepository;
    private final TDataApiService tDataApiService;
    private final SignalCacheMapper signalCacheMapper;
    private final RedissonClient redissonClient;

    private final ConcurrentHashMap<Integer, ReentrantLock> lockMap = new ConcurrentHashMap<>();

    private static final long MAX_CACHE_WAIT_MS = 500;    // 최대 대기 시간 (0.5초)
    private static final long POLL_INTERVAL_MS = 50;    // 폴링 주기 (0.05초)

    public CrossroadWithSignalResponse getCrossroadWithSignals(Integer crossroadId) {
        log.debug("getCrossroadWithSignals 호출: crossroadId={}", crossroadId);
        Crossroad findCrossroad = crossroadJpaRepository.findById(crossroadId)
                .orElseThrow(() -> new CustomException(ErrorCode.CROSSROAD_NOT_FOUND));
        log.debug("찾은 교차로: id={}, name={}", findCrossroad.getCrossroadId(), findCrossroad.getName());

        //Redis에 캐시된 데이터가 존재하면 반환, 없으면 API 호출
        SignalCache findCache = getOrFetchCache(crossroadId);

        List<SignalCacheResponse> signalCacheResponses = findCache.getSignals().stream()
                .map(this::toSignalCacheResponse)
                .toList();
        log.debug("매핑된 SignalCacheResponse 개수={}", signalCacheResponses.size());

        return new CrossroadWithSignalResponse(
                findCrossroad.getCrossroadId(),
                findCrossroad.getName(),
                findCrossroad.getLat(),
                findCrossroad.getLng(),
                findCache.getSendAt(),
                findCache.getCachedAt(),
                signalCacheResponses
        );
    }

//    private SignalCache getOrFetchCache(Integer crossroadId) {
//        SignalCache findCache = signalCacheRepository.findById(crossroadId)
//                .orElseGet(() -> fetchAndCacheSignal(crossroadId)); //캐시된 신호 데이터가 없는 경우
//        log.debug("사용할 SignalCache: crossroadId={}, sendAt={}, cachedAt={}",
//                findCache.getCrossroadId(), findCache.getSendAt(), findCache.getCachedAt());
//        return findCache;
//    }
//
//    private SignalCache getOrFetchCacheV2(Integer crossroadId) {
//        Optional<SignalCache> optionalCache = signalCacheRepository.findById(crossroadId);
//
//        if (optionalCache.isPresent()) { //캐시된 데이터가 존재하면 리턴
//            return optionalCache.get();
//        }
//
//        ReentrantLock lock = lockMap.computeIfAbsent(crossroadId, id -> new ReentrantLock());
//        lock.lock();
//        try {
//            //Double-Checking
//            optionalCache = signalCacheRepository.findById(crossroadId);
//            return optionalCache.orElseGet(() ->
//                    fetchAndCacheSignal(crossroadId));
//        } finally {
//            lock.unlock();
//        }
//    }
//
//    private SignalCache getOrFetchCacheV4(Integer crossroadId) {
//        Optional<SignalCache> optionalCache = signalCacheRepository.findById(crossroadId);
//        if (optionalCache.isPresent()) {
//            return optionalCache.get();
//        }
//
//        ReentrantLock lock = lockMap.computeIfAbsent(crossroadId, id -> new ReentrantLock());
//
//        boolean locked = false;
//        try {
//            //TryLock 시도
//            locked = lock.tryLock(100, TimeUnit.MILLISECONDS);
//            if (locked) {
//                //Lock 획득 성공 → **두 번째 캐시 조회** (더블 체크)
//                optionalCache = signalCacheRepository.findById(crossroadId);
//                return optionalCache.orElseGet(() ->
//                        fetchAndCacheSignal(crossroadId));
//            } else {
//                //Lock 획득 실패 → 다른 스레드가 이미 캐싱했을 가능성 다시 확인
//                optionalCache = signalCacheRepository.findById(crossroadId);
//                if (optionalCache.isPresent()) {
//                    return optionalCache.get();
//                }
//                //그래도 없으면 폴링 혹은 즉시 예외
//                long waitUntil = System.currentTimeMillis() + MAX_CACHE_WAIT_MS;
//                while (System.currentTimeMillis() < waitUntil) {
//                    optionalCache = signalCacheRepository.findById(crossroadId);
//                    if (optionalCache.isPresent()) {
//                        return optionalCache.get();
//                    }
//                    Thread.sleep(POLL_INTERVAL_MS);
//                }
//                throw new CustomException(ErrorCode.EXTERNAL_SIGNAL_API_ERROR);
//            }
//        } catch (InterruptedException e) {
//            Thread.currentThread().interrupt();
//            throw new CustomException(ErrorCode.EXTERNAL_SIGNAL_API_ERROR);
//        } finally {
//            if (locked) {
//                lock.unlock();
//            }
//        }
//    }

    private SignalCache getOrFetchCache(Integer crossroadId) {
        Optional<SignalCache> optionalCache = signalCacheRepository.findById(crossroadId);
        if (optionalCache.isPresent()) {
            return optionalCache.get();
        }

        String lockKey = "lock:crossroad:" + crossroadId;
        RLock rlock = redissonClient.getLock(lockKey);
        boolean locked = false;

        final long lockWaitTimeMs = 10; //Lock 대기 시작
        final long lockLeaseTimeMs = 2_000; //Lock 점유 시간
        final long maxCacheWaitMs = 150; //Cache polling 최대 주기
        final long pollIntervalMs = 20; //Cache polling 주기

        try {
            locked = rlock.tryLock(lockWaitTimeMs, lockLeaseTimeMs, TimeUnit.MILLISECONDS);
            if (locked) {
                //double check
                optionalCache = signalCacheRepository.findById(crossroadId);
                return optionalCache.orElseGet(() ->
                        fetchAndCacheSignal(crossroadId));
            } else {
                //Lock 점유 실패시 폴링 대기
                long waitUntil = System.currentTimeMillis() + maxCacheWaitMs;
                while (System.currentTimeMillis() < waitUntil) {
                    optionalCache = signalCacheRepository.findById(crossroadId);
                    if (optionalCache.isPresent()) {
                        return optionalCache.get();
                    }
                    Thread.sleep(pollIntervalMs);
                }
                throw new CustomException(ErrorCode.EXTERNAL_SIGNAL_API_ERROR);
            }

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CustomException(ErrorCode.EXTERNAL_SIGNAL_API_ERROR);
        } finally {
            if (locked && rlock.isHeldByCurrentThread()) {
                rlock.unlock();
            }
        }
    }


    private SignalCache fetchAndCacheSignal(Integer crossroadId) {
        //외부 API 호출
        log.debug("fetchAndCacheSignal 호출: crossroadId={}", crossroadId);
        List<TDataSignalResponse> responses = tDataApiService.getSignalsByCrossroadId(crossroadId);

        //가장 최신 데이터만 사용
        if (responses.isEmpty()) {
            log.warn("TData API 응답 없음 or 빈 리스트: crossroadId={}", crossroadId);
            throw new CustomException(ErrorCode.EXTERNAL_SIGNAL_API_ERROR);
        }
        TDataSignalResponse latest = responses.get(0);

        //API 응답을 Redis 캐시
        SignalCache cache = signalCacheMapper.fromTDataResponse(latest);
        SignalCache savedCache = signalCacheRepository.save(cache);
        log.debug("SignalCache 저장 완료: crossroadId={}", savedCache.getCrossroadId());

        return cache;
    }

    private SignalCacheResponse toSignalCacheResponse(SignalInfo info) {
        return new SignalCacheResponse(
                info.getDirection(),
                info.getStatus(),
                info.getRemainingSec()
        );
    }

    /**
     * 범위 내 위/경도 조건으로 교차로 리스트를 조회
     *
     * @param request 범위 정보(SW/NE 좌표)
     * @return 해당 범위 교차로 DTO 리스트
     * @throws CustomException 조회 결과가 없으면 오류 발생
     */
    public List<CrossroadRangeResponse> getCrossroadsInRange(CrossroadRangeRequest request) {
        List<Crossroad> crossroadList = crossroadJpaRepository.findByLatBetweenAndLngBetween(
                request.swLat(), request.neLat(),
                request.swLng(), request.neLng()
        );

        if (crossroadList.isEmpty()) { //교차로가 존재하지 않을 경우
            throw new CustomException(ErrorCode.CROSSROAD_RANGE_EMPTY);
        }

        List<CrossroadRangeResponse> result = new ArrayList<>();
        for (Crossroad crossroad : crossroadList) {
            result.add(new CrossroadRangeResponse(
                    crossroad.getCrossroadId(),
                    crossroad.getName(),
                    crossroad.getLat(),
                    crossroad.getLng()
            ));
        }
        return result;
    }
}
