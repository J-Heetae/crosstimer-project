package com.goose.crosstimer.signal.service;

import com.goose.crosstimer.api.client.TDataApiClient;
import com.goose.crosstimer.api.dto.TDataRequest;
import com.goose.crosstimer.api.dto.TDataSignalResponse;
import com.goose.crosstimer.common.dto.SignalData;
import com.goose.crosstimer.common.util.SignalMappingUtil;
import com.goose.crosstimer.signal.dto.SignalCache;
import com.goose.crosstimer.signal.dto.SignalResponse;
import com.goose.crosstimer.signal.repository.SignalInfoRepository;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class SignalService {
    private static final Duration EMPTY_TTL = Duration.ofMinutes(5);

    private final RedisTemplate<String, SignalCache> redisTemplate;
    private final RedissonClient redissonClient;
    private final TDataApiClient apiClient;
    private final SignalInfoRepository signalInfoRepository;

    public SignalResponse getSignalByItstId(Integer itstId) {
        String cacheKey = buildCacheKey(itstId);

        //Redis 캐시 조회
        SignalCache cache = redisTemplate.opsForValue().get(cacheKey);
        if (cache != null) {
            return toSignalResponse(cache);
        }

        RLock lock = redissonClient.getLock(buildLockKey(itstId));
        lock.lock();
        try {
            cache = redisTemplate.opsForValue().get(cacheKey);
            if (cache != null) { //다시 캐시 확인, 다른 쓰레드가 채웠는지 확인
                return toSignalResponse(cache);
            }
            //API 호출
            Map<String, SignalData> signals = fetchSignals(itstId);

            //SignalCache 생성
            cache = new SignalCache(signals, Instant.now());

            //Redis에 저장
            saveCacheToRedis(cacheKey, cache);

            return toSignalResponse(cache);

        } finally {
            lock.unlock();
        }
    }

    private void saveCacheToRedis(String cacheKey, SignalCache cache) {
        if (cache.getSignals().isEmpty()) {
            redisTemplate.opsForValue().set(cacheKey, cache, EMPTY_TTL);
        } else {
            long ttlSec = cache.getSignals().values().stream()
                    .mapToInt(SignalData::remainingDeciSeconds)
                    .min().orElse(0) / 10L + 1;
            redisTemplate.opsForValue().set(cacheKey, cache, Duration.ofSeconds(ttlSec));
        }
    }

    private Map<String, SignalData> fetchSignals(int itstId) {
        List<TDataSignalResponse> responseList = apiClient.getSignalInfo(
                TDataRequest.fromItstId(itstId));

        if (!responseList.isEmpty()) {
            TDataSignalResponse response = responseList.stream()
                    .max(Comparator.comparingLong(TDataSignalResponse::trsmUtcTime))
                    .get();
            return SignalMappingUtil.toSignalMap(response);
        }

        //API 실패시
        return signalInfoRepository
                .findById(itstId)
                .map(SignalMappingUtil::toSignalMap)
                .orElseGet(Collections::emptyMap);
    }

    private SignalResponse toSignalResponse(SignalCache cache) {
        return new SignalResponse(cache.getSignals());
    }

    private String buildCacheKey(Integer itstId) {
        return "signal:" + itstId;
    }

    private String buildLockKey(Integer itstId) {
        return "lock:signal:" + itstId;
    }
}
