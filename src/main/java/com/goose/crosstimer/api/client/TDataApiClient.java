package com.goose.crosstimer.api.client;

import com.goose.crosstimer.api.dto.TDataApiRequestDto;
import com.goose.crosstimer.api.dto.MapResponseDto;
import com.goose.crosstimer.api.dto.SignalResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriBuilder;

import java.net.URI;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TDataApiClient {
    private final WebClient webClient;

    @Value("${tdata.api-key}")
    private String apiKey;

    public List<MapResponseDto> getMapInfo(TDataApiRequestDto requestDto) {
        return webClient.get()
                .uri(uriBuilder -> applyCommonParams(uriBuilder
                                .scheme("http")
                                .host("t-data.seoul.go.kr")
                                .path("/apig/apiman-gateway/tapi/v2xCrossroadMapInformation/1.0"),
                        requestDto))
                .retrieve()
                .bodyToFlux(MapResponseDto.class)
                .collectList()
                .block();
    }

    public List<SignalResponseDto> getSignalInfo(TDataApiRequestDto requestDto) {

        return webClient.get()
                .uri(uriBuilder -> applyCommonParams(uriBuilder
                                .scheme("https")
                                .host("t-data.seoul.go.kr")
                                .path("/apig/apiman-gateway/tapi/v2xSignalPhaseTimingFusionInformation/1.0"),
                        requestDto))
                .retrieve()
                .bodyToFlux(SignalResponseDto.class)
                .collectList()
                .block();
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
