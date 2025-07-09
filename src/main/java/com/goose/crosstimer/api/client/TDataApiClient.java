package com.goose.crosstimer.api.client;

import com.goose.crosstimer.api.dto.TDataRequest;
import com.goose.crosstimer.api.dto.TDataCrossroadResponse;
import com.goose.crosstimer.api.dto.TDataSignalResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TDataApiClient {
    private final WebClient webClient;

    private static final Duration DEFAULT_TIMEOUT_SECONDS = Duration.ofSeconds(1L);
    private static final String BASE_URL = "https://t-data.seoul.go.kr";

    @Value("${tdata.api-key}")
    private String apiKey;

    public List<TDataCrossroadResponse> getCrossroadInfo(TDataRequest dto) {
        return fetch(
                "/apig/apiman-gateway/tapi/v2xCrossroadMapInformation/1.0",
                dto,
                TDataCrossroadResponse.class,
                "교차로 Map 정보 API 호출"
        );
    }

    public List<TDataSignalResponse> getSignalInfo(TDataRequest dto) {
        return fetch(
                "/apig/apiman-gateway/tapi/v2xSignalPhaseTimingFusionInformation/1.0",
                dto,
                TDataSignalResponse.class,
                "신호제어기 신호 잔여시간 정보서비스 API 호출"
        );
    }

    // ==== 공통 로직 ==== //

    private <T> List<T> fetch(String path, TDataRequest requestDto, Class<T> responseType, String logMessage) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> applyCommonParams(
                            uriBuilder
                                    .scheme(URI.create(BASE_URL).getScheme())
                                    .host(URI.create(BASE_URL).getHost())
                                    .path(path),
                            requestDto))
                    .retrieve()
                    .bodyToFlux(responseType)
                    .doOnSubscribe(sub -> log.info("{}", logMessage))
                    .collectList()
                    .block(DEFAULT_TIMEOUT_SECONDS);
        } catch (Exception e) {
            log.warn("{} 실패, 빈 리스트 반환", logMessage, e);
            return List.of();
        }
    }

    private URI applyCommonParams(UriBuilder builder, TDataRequest dto) {
        builder.queryParam("apikey", apiKey)
                .queryParam("type", dto.getType())
                .queryParam("pageNo", dto.getPageNo())
                .queryParam("numOfRows", dto.getNumOfRows());

        if (dto.getCrossroadId() != null) {
            builder.queryParam("itstId", dto.getCrossroadId());
        }

        return builder.build();
    }
}
