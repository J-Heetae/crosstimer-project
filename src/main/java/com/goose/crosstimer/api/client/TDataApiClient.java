package com.goose.crosstimer.api.client;

import com.goose.crosstimer.api.dto.TDataRequest;
import com.goose.crosstimer.api.dto.TDataCrossroadResponse;
import com.goose.crosstimer.api.dto.TDataSignalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TDataApiClient {
    private final WebClient webClient;

    private static final String BASE_URL = "https://t-data.seoul.go.kr";
    private static final int DEFAULT_TIMEOUT_SECONDS = 10;

    @Value("${tdata.api-key}")
    private String apiKey;

    public List<TDataCrossroadResponse> getCrossroadInfo(TDataRequest requestDto) {
        return webClient.get()
                .uri(uriBuilder -> applyCommonParams(uriBuilder
                                .scheme(URI.create(BASE_URL).getScheme())
                                .host(URI.create(BASE_URL).getHost())
                                .path("/apig/apiman-gateway/tapi/v2xCrossroadMapInformation/1.0"),
                        requestDto))
                .retrieve()
                .bodyToFlux(TDataCrossroadResponse.class)
                .doOnSubscribe(sub -> System.out.println("교차로 MAP API 호출 시작"))
                .doOnNext(dto -> System.out.println("받은 응답: " + dto))
                .doOnError(error -> System.out.println("에러 발생: " + error.getMessage()))
                .collectList()
                .block(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS));
    }

    public List<TDataSignalResponse> getSignalInfo(TDataRequest requestDto) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> applyCommonParams(uriBuilder
                                    .scheme(URI.create(BASE_URL).getScheme())
                                    .host(URI.create(BASE_URL).getHost())
                                    .path("/apig/apiman-gateway/tapi/v2xSignalPhaseTimingFusionInformation/1.0"),
                            requestDto))
                    .retrieve()
                    .bodyToFlux(TDataSignalResponse.class)
                    .doOnSubscribe(sub -> System.out.println("신호 잔여시간 정보 API 호출 시작"))
                    .doOnNext(dto -> System.out.println("받은 응답: " + dto))
                    .doOnError(error -> System.out.println("에러 발생: " + error.getMessage()))
                    .collectList()
                    .block(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS));
        } catch (Exception e) {
            System.out.println("[" + requestDto.getItstId() + "] API 호출 실패: " + e.getMessage());
            return List.of();
        }
    }

    public Mono<Optional<TDataSignalResponse>> getFirstSignalByItstIdAsync(Integer itstId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .scheme(URI.create(BASE_URL).getScheme())
                        .host(URI.create(BASE_URL).getHost())
                        .path("/apig/apiman-gateway/tapi/v2xSignalPhaseTimingFusionInformation/1.0")
                        .queryParam("apikey", apiKey)
                        .queryParam("itstId", itstId)
                        .build())
                .retrieve()
                .bodyToFlux(TDataSignalResponse.class)
                .next() // 가장 첫 번째 응답 하나만
                .timeout(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS)) // 응답 제한 시간
                .map(Optional::of)
                .onErrorResume(e -> Mono.just(Optional.empty())); // 실패 시 빈 값 반환
    }

    private URI applyCommonParams(UriBuilder builder, TDataRequest requestDto) {
        builder.queryParam("apikey", apiKey)
                .queryParam("type", requestDto.getType())
                .queryParam("pageNo", requestDto.getPageNo())
                .queryParam("numOfRows", requestDto.getNumOfRows());

        if (requestDto.getItstId() != null) {
            builder.queryParam("itstId", requestDto.getItstId());
        }

        return builder.build();
    }
}
