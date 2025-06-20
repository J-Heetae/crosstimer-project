package com.goose.crosstimer.api.client;

import com.goose.crosstimer.api.dto.TDataRequest;
import com.goose.crosstimer.api.dto.TDataCrossroadResponse;
import com.goose.crosstimer.api.dto.TDataSignalResponse;
import com.goose.crosstimer.common.exception.CustomException;
import com.goose.crosstimer.common.exception.ErrorCode;
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

    private static final String BASE_URL = "https://t-data.seoul.go.kr";
    private static final int DEFAULT_TIMEOUT_SECONDS = 60;

    @Value("${tdata.api-key}")
    private String apiKey;

    public List<TDataCrossroadResponse> getCrossroadInfo(TDataRequest requestDto) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> applyCommonParams(uriBuilder
                                    .scheme(URI.create(BASE_URL).getScheme())
                                    .host(URI.create(BASE_URL).getHost())
                                    .path("/apig/apiman-gateway/tapi/v2xCrossroadMapInformation/1.0"),
                            requestDto))
                    .retrieve()
                    .bodyToFlux(TDataCrossroadResponse.class)
                    .doOnSubscribe(sub -> log.info("교차로 Map 정보 API 호출"))
                    .doOnError(error -> log.error("API 호출 실패, 에러 발생: {}", error.getMessage()))
                    .collectList()
                    .block(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS));
        } catch (Exception e) {
            throw new CustomException(ErrorCode.EXTERNAL_CROSSROAD_API_ERROR, e);
        }
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
                    .doOnSubscribe(sub -> log.info("신호제어기 신호 잔여시간 정보서비스 API 호출"))
                    .doOnError(error -> log.error("API 호출 실패, 에러 발생: {}", error.getMessage()))
                    .collectList()
                    .block(Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS));
        } catch (Exception e) {
            throw new CustomException(ErrorCode.EXTERNAL_SIGNAL_API_ERROR, e);
        }
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
