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
    @DisplayName("동시 호출 시 외부 API 호출 중복 발생")
    void concurrent_shouldCallApiForEachThread() throws InterruptedException {
        final int threadCount = 50; //쓰레드 수
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        long startNanos = System.nanoTime();
        for (int i = 0; i < threadCount; i++) {
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
        System.out.println("총 소요시간: " + elapsedMs + "ms");

        //WireMock이 기록한 ServeEvent 개수로 외부 API 호출 횟수 검증
        int actualApiCalls = wireMockServer.getAllServeEvents().size();
        System.out.println("API 호출 횟수: " + actualApiCalls);
        assertThat(actualApiCalls)
                .as("스레드 수만큼 API가 호출되어야 한다.")
                .isEqualTo(threadCount);

        executor.shutdownNow();
    }

}