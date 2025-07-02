package com.goose.crosstimer.signal.repository;

import com.goose.crosstimer.signal.domain.SignalCache;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class SignalCacheRepositoryImpl implements SignalCacheRepositoryCustom {
    private final RedisTemplate<String, SignalCache> redisTemplate;

    @Override
    public void saveAllPipeline(List<SignalCache> caches) {
        redisTemplate.executePipelined((RedisCallback<?>) (connection) -> {
            for (SignalCache cache : caches) {
                // opsForValue().set은 자동으로 직렬화
                redisTemplate.opsForValue().set(cache.getId(), cache);
            }
            return null;
        });
    }

    @Override
    public List<SignalCache> findByItstId(Long itstId) {
        String key = "signal:" + itstId + ":*";

        Set<String> keys = redisTemplate.keys(key);

        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }

        return redisTemplate.opsForValue().multiGet(keys);
    }
}

