package com.goose.crosstimer.signal.service;

import com.goose.crosstimer.api.client.TDataApiClient;
import com.goose.crosstimer.api.dto.TDataRequest;
import com.goose.crosstimer.api.dto.TDataSignalResponse;
import com.goose.crosstimer.common.dto.SignalData;
import com.goose.crosstimer.signal.domain.SignalInfo;
import com.goose.crosstimer.signal.dto.SignalCache;
import com.goose.crosstimer.signal.dto.SignalResponse;
import com.goose.crosstimer.signal.repository.SignalInfoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SignalServiceTest {

    @Mock
    private RedisTemplate<String, SignalCache> redisTemplate;
    @Mock
    private ValueOperations<String, SignalCache> valueOperations;
    @Mock
    private RedissonClient redissonClient;
    @Mock
    private RLock rLock;
    @Mock
    private TDataApiClient apiClient;
    @Mock
    private SignalInfoRepository signalInfoRepository;

    @InjectMocks
    private SignalService signalService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void testGetSignalByItstId_CacheHit() {
        //Given
        final Integer itstId = 1;
        Map<String, SignalData> map = Map.of("n", new SignalData(275, "stop-And-Remain"));
        SignalCache cache = new SignalCache(map, Instant.now());
        when(valueOperations.get("signal:" + itstId)).thenReturn(cache);

        //When
        SignalResponse findResponse = signalService.getSignalByItstId(itstId);

        //Then
        assertThat(findResponse.signals()).containsKey("n");
        verify(apiClient, never()).getSignalInfo(any(TDataRequest.class));
    }

    @Test
    void testGetSignalByItstId_CacheMiss_ApiFetch() {
        //Given
        when(redissonClient.getLock(anyString())).thenReturn(rLock);

        final Integer itstId = 1596;
        when(valueOperations.get("signal:" + itstId)).thenReturn(null);

        TDataSignalResponse apiResponse = new TDataSignalResponse(
                "SPAT-CIB1101159600-1750061167-594277",
                itstId,
                "CIB1101159600",
                2025,
                6,
                17,
                5,
                65900,
                982,
                1750107599982L,
                240299,
                599,
                "v2x",
                OffsetDateTime.parse("2025-06-16T21:00:00Z"),
                null, null,   // ntPdsgRmdrCs, ntPdsgStatNm
                null, null,   // etPdsgRmdrCs, etPdsgStatNm
                null, null,   // stPdsgRmdrCs, stPdsgStatNm
                null, null,   // wtPdsgRmdrCs, wtPdsgStatNm
                1096, "stop-And-Remain",  // nePdsgRmdrCs, nePdsgStatNm
                426, "stop-And-Remain",   // sePdsgRmdrCs, sePdsgStatNm
                1096, "stop-And-Remain",  // swPdsgRmdrCs, swPdsgStatNm
                null, null    // nwPdsgRmdrCs, nwPdsgStatNm
        );
        when(apiClient.getSignalInfo(any(TDataRequest.class))).thenReturn(List.of(apiResponse));

        //When
        SignalResponse findResponse = signalService.getSignalByItstId(itstId);

        //Then
        assertThat(findResponse.signals()).containsEntry("ne", new SignalData(1096, "stop-And-Remain"));
        assertThat(findResponse.signals()).containsEntry("se", new SignalData(426, "stop-And-Remain"));
        assertThat(findResponse.signals()).containsEntry("sw", new SignalData(1096, "stop-And-Remain"));
        verify(valueOperations).set(anyString(), any(SignalCache.class), any(Duration.class));
    }

    @Test
    void testGetSignalByItstId_CacheMiss_ApiFail_FallbackToDB() {
        //Given
        when(redissonClient.getLock(anyString())).thenReturn(rLock);

        final Integer itstId = 1596;
        final Integer remainingDeciSeconds = 120;
        final String status = "protected-Movement-Allowed";

        when(valueOperations.get("signal:" + itstId)).thenReturn(null);
        when(apiClient.getSignalInfo(any(TDataRequest.class))).thenReturn(List.of());

        SignalInfo mockSignalInfo = mock(SignalInfo.class);
        when(mockSignalInfo.getEtPdsgRmdrCs()).thenReturn(remainingDeciSeconds);
        when(mockSignalInfo.getEtPdsgStatNm()).thenReturn(status);
        when(signalInfoRepository.findById(itstId))
                .thenReturn(Optional.of(mockSignalInfo));

        //When
        SignalResponse findResponse = signalService.getSignalByItstId(itstId);

        //Then
        assertThat(findResponse.signals()).containsEntry("e", new SignalData(remainingDeciSeconds, status));
        verify(valueOperations).set(anyString(), any(SignalCache.class), any(Duration.class));
    }

    @Test
    void testGetSignalByItstId_ConcurrentRequests() throws Exception {
        //Given
        when(redissonClient.getLock(anyString())).thenReturn(rLock);

        final int itstId = 1596;
        final Integer remainingDeciSeconds = 120;
        final String status = "protected-Movement-Allowed";

        String key = "signal:" + itstId;
        AtomicBoolean cache = new AtomicBoolean(false);

        when(valueOperations.get(key)).thenAnswer(inv -> cache.get() ? new SignalCache(Map.of("n", new SignalData(remainingDeciSeconds, status)), Instant.now()) : null);
        doAnswer(inv -> {
            cache.set(true);
            return null;
        }).when(valueOperations).set(eq(key), any(SignalCache.class), any(Duration.class));

        TDataSignalResponse apiResponse = new TDataSignalResponse(
                "SPAT-CIB1101159600-1750061167-594277",
                itstId,
                "CIB1101159600",
                2025,
                6,
                17,
                5,
                65900,
                982,
                1750107599982L,
                240299,
                599,
                "v2x",
                OffsetDateTime.parse("2025-06-16T21:00:00Z"),
                remainingDeciSeconds, status,   // ntPdsgRmdrCs, ntPdsgStatNm
                null, null,   // etPdsgRmdrCs, etPdsgStatNm
                null, null,   // stPdsgRmdrCs, stPdsgStatNm
                null, null,   // wtPdsgRmdrCs, wtPdsgStatNm
                null, null,  // nePdsgRmdrCs, nePdsgStatNm
                null, null,   // sePdsgRmdrCs, sePdsgStatNm
                null, null,  // swPdsgRmdrCs, swPdsgStatNm
                null, null    // nwPdsgRmdrCs, nwPdsgStatNm
        );
        when(apiClient.getSignalInfo(any(TDataRequest.class)))
                .thenAnswer(inv -> {
                    Thread.sleep(200);
                    return List.of(apiResponse);
                });

        java.util.concurrent.locks.ReentrantLock realLock = new java.util.concurrent.locks.ReentrantLock();
        doAnswer(inv -> {
            realLock.lock();
            return null;
        }).when(rLock).lock();
        doAnswer(inv -> {
            realLock.unlock();
            return null;
        }).when(rLock).unlock();

        //When
        ExecutorService exec = Executors.newFixedThreadPool(100);
        List<Future<SignalResponse>> futures = IntStream.range(0, 100)
                .mapToObj(i -> exec.submit(() -> signalService.getSignalByItstId(itstId)))
                .toList();

        // 결과 검증
        for (Future<SignalResponse> f : futures) {
            SignalResponse resp = f.get();
            assertThat(resp.signals()).containsEntry("n", new SignalData(remainingDeciSeconds, status));
        }

        verify(apiClient, times(1)).getSignalInfo(any(TDataRequest.class));
        exec.shutdown();
    }
}