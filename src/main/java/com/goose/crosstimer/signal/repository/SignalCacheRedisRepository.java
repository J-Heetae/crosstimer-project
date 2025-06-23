package com.goose.crosstimer.signal.repository;

import com.goose.crosstimer.signal.domain.SignalCache;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Map;

@Repository
public class SignalCacheRedisRepository {
    private final RedisTemplate<String, SignalCache> redisTemplate;
    private static final Duration TTL = Duration.ofMinutes(5);

    public SignalCacheRedisRepository(RedisTemplate<String, SignalCache> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    private String buildKey(Integer itstId, String direction) {
        return "signal:" + itstId + ":" + direction.toUpperCase();
    }

    /**
     * 단일 캐시 저장
     */
    public void save(Integer itstId, String direction, SignalCache cache) {
        String key = buildKey(itstId, direction);
        redisTemplate.opsForValue().set(key, cache, TTL);
    }


    public void saveAll(Map<Integer, Map<String, SignalCache>> batch) {
        batch.forEach((itstId, dirMap) -> {
            dirMap.forEach((direction, cache) -> save(itstId, direction, cache));
        });
    }

    public SignalCache findByItstIdAndDirection(Integer itstId, String direction) {
        return redisTemplate.opsForValue().get(buildKey(itstId, direction));
    }

    public void delete(Integer itstId, String direction) {
        redisTemplate.delete(buildKey(itstId, direction));
    }
}
