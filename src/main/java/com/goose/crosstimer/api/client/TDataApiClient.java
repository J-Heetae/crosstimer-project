package com.goose.crosstimer.api.client;

import com.goose.crosstimer.api.dto.TDataApiRequestDto;
import com.goose.crosstimer.api.dto.CrossroadResponseDto;
import com.goose.crosstimer.api.dto.SignalResponseDto;
import io.netty.util.Signal;
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

    private static final int API_WAITING_TIME = 3;

    @Value("${tdata.api-key}")
    private String apiKey;

    public List<CrossroadResponseDto> getCrossroadInfo(TDataApiRequestDto requestDto) {
        return webClient.get()
                .uri(uriBuilder -> applyCommonParams(uriBuilder
                                .scheme("https")
                                .host("t-data.seoul.go.kr")
                                .path("/apig/apiman-gateway/tapi/v2xCrossroadMapInformation/1.0"),
                        requestDto))
                .retrieve()
                .bodyToFlux(CrossroadResponseDto.class)
                .doOnSubscribe(sub -> System.out.println("교차로 MAP API 호출 시작"))
                .doOnNext(dto -> System.out.println("받은 응답: " + dto))
                .doOnError(error -> System.out.println("에러 발생: " + error.getMessage()))
                .collectList()
                .block();
    }

    public List<SignalResponseDto> getSignalInfo(TDataApiRequestDto requestDto) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> applyCommonParams(uriBuilder
                                    .scheme("https")
                                    .host("t-data.seoul.go.kr")
                                    .path("/apig/apiman-gateway/tapi/v2xSignalPhaseTimingFusionInformation/1.0"),
                            requestDto))
                    .retrieve()
                    .bodyToFlux(SignalResponseDto.class)
                    .doOnSubscribe(sub -> System.out.println("신호 잔여시간 정보 API 호출 시작"))
                    .doOnNext(dto -> System.out.println("받은 응답: " + dto))
                    .doOnError(error -> System.out.println("에러 발생: " + error.getMessage()))
                    .collectList()
                    .block(Duration.ofSeconds(API_WAITING_TIME));
        } catch (Exception e) {
            System.out.println("[" + requestDto.getItstId() + "] API 호출 실패: " + e.getMessage());
            return List.of();
        }
    }

    public Mono<Optional<SignalResponseDto>> getFirstSignalByItstIdAsync(Integer itstId) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.scheme("https")
                        .host("t-data.seoul.go.kr")
                        .path("/apig/apiman-gateway/tapi/v2xSignalPhaseTimingFusionInformation/1.0")
                        .queryParam("apikey", apiKey)
                        .queryParam("itstId", itstId)
                        .build())
                .retrieve()
                .bodyToFlux(SignalResponseDto.class)
                .next() // 가장 첫 번째 응답 하나만
                .timeout(Duration.ofSeconds(2)) // 응답 제한 시간
                .map(Optional::of)
                .onErrorResume(e -> Mono.just(Optional.empty())); // 실패 시 빈 값 반환
    }

    private URI applyCommonParams(UriBuilder builder, TDataApiRequestDto requestDto) {
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
