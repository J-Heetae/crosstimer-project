package com.goose.crosstimer.signal.service;

import com.goose.crosstimer.crossroad.domain.Crossroad;
import com.goose.crosstimer.signal.domain.SignalCache;
import com.goose.crosstimer.signal.domain.SignalCycle;
import com.goose.crosstimer.signal.domain.SignalLog;
import com.goose.crosstimer.signal.repository.SignalCycleJpaRepository;
import com.goose.crosstimer.signal.service.SignalCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SignalCacheServiceUnitTest {

    @Mock
    RedisTemplate<String, SignalCache> redisTemplate;
    @Mock
    ValueOperations<String, SignalCache> valueOps;
    @Mock
    SignalCycleJpaRepository cycleRepo;

    @InjectMocks
    SignalCacheService cacheService;

    @BeforeEach
    void setUp() {
        // RedisTemplate.opsForValue()가 valueOps를 반환하도록 설정
        when(redisTemplate.opsForValue()).thenReturn(valueOps);

        Crossroad crossroad = Crossroad.builder()
                .itstId(1)
                .build();

        // 사이클 저장소에 하나의 SignalCycle 리턴
        SignalCycle cycle = SignalCycle.builder()
                .crossroad(crossroad)
                .direction("N")
                .greenSec(10)
                .redSec(20)
                .build();
        when(cycleRepo.findAllWithCrossroad()).thenReturn(List.of(cycle));
    }

    @Test
    void cacheSignal_shouldCallRedisSetOnce() {
        // given
        SignalLog log1 = SignalLog.builder()
                .itstId(1)
                .direction("N")
                .signalTimestamp(Instant.now().minusSeconds(5))
                .status("GREEN")
                .remaining(5)
                .build();
        SignalLog log2 = SignalLog.builder()
                .itstId(1)
                .direction("N")
                .signalTimestamp(Instant.now())
                .status("RED")
                .remaining(15)
                .build();

        // when
        cacheService.cacheSignal(List.of(log1, log2));

        // then
        String expectedKey = "signal:1:N";
        // 가장 마지막 로그를 기준으로 한 캐시 객체
        ArgumentCaptor<SignalCache> captor = ArgumentCaptor.forClass(SignalCache.class);
        verify(valueOps, times(1)).set(eq(expectedKey), captor.capture());
        SignalCache saved = captor.getValue();

        assertEquals("RED", saved.getStatus());
        assertEquals(15, saved.getRemaining());
        assertEquals(10, saved.getPredictedGreenSec());
        assertEquals(20, saved.getPredictedRedSec());
    }
}
