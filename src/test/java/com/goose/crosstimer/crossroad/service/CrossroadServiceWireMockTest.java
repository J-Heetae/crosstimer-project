package com.goose.crosstimer.crossroad.service;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.goose.crosstimer.crossroad.dto.CrossroadWithSignalResponse;
import com.goose.crosstimer.signal.repository.SignalCacheRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@AutoConfigureWireMock(port = 0, stubs = "classpath:mappings")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CrossroadServiceWireMockTest {
    @Autowired
    private CrossroadService crossroadService;

    @Autowired
    private SignalCacheRepository signalCacheRepository;

    @Autowired
    private WireMockServer wireMockServer;

    @BeforeEach
    void setup() {
        //Redis 캐시 클리어
        signalCacheRepository.deleteAll();
        //WireMockServer 기록 초기화
        wireMockServer.resetRequests();
    }

    @Test
    @DisplayName("redisson 활용한 동시성 제어")
    void concurrent_Redisson() throws InterruptedException {
        final int threadCount = 50; //쓰레드 수
        final int count = 10;
        long totalMs = 0L;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < count; i++) {
            System.out.println((i + 1) + "번째 테스트 시작 =================================");
            CountDownLatch latch = new CountDownLatch(threadCount);
            long startNanos = System.nanoTime();
            for (int j = 0; j < threadCount; j++) {
                executor.submit(() -> {
                    try {
                        CrossroadWithSignalResponse response = crossroadService.getCrossroadWithSignals(123);
                        assertThat(response.crossroadId()).isEqualTo(123);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            // 모든 스레드 완료 대기
            boolean finished = latch.await(100, TimeUnit.SECONDS);
            assertThat(finished).isTrue();

            long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000;
            if (i != 0) {
                System.out.println((i + 1) + "번째 실행 시간: " + elapsedMs + "ms");
                totalMs += elapsedMs;
            }
            int actualApiCalls = wireMockServer.getAllServeEvents().size();
            System.out.println((i + 1) + "번째 API 호출 횟수: " + actualApiCalls);
            assertThat(actualApiCalls)
                    .as("각 시도당 1번만 API를 호출 한다.")
                    .isEqualTo(1);
            //Redis 캐시 클리어
            signalCacheRepository.deleteAll();
            //WireMockServer 기록 초기화
            wireMockServer.resetRequests();
        }

        System.out.println("평균 실행 시간: " + (totalMs / count - 1) + "ms");

        executor.shutdownNow();
    }

}