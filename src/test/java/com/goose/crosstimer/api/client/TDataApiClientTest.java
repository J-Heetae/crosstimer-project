package com.goose.crosstimer.api.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.goose.crosstimer.api.dto.TDataCrossroadResponse;
import com.goose.crosstimer.api.dto.TDataSignalResponse;
import com.goose.crosstimer.api.dto.TDataRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TDataApiClientTest {

    @Autowired
    TDataApiClient tDataApiClient;

    @Test
    void 교차로MAP_API_기본요청_테스트() throws JsonProcessingException {
        List<TDataCrossroadResponse> mapInfo = tDataApiClient.getCrossroadInfo(TDataRequest.from());

//        mapInfo.forEach(System.out::println);

        assertNotNull(mapInfo);
        assertFalse(mapInfo.isEmpty());
        assertThat(mapInfo.size()).isEqualTo(1000);
    }

    @Test
    void 교차로MAP_API_페이지네이션_테스트() {
        final int pageNo = 1;
        final int numOfRows = 10;
        List<TDataCrossroadResponse> mapInfo = tDataApiClient.getCrossroadInfo(TDataRequest.fromPagination(pageNo, numOfRows));

//        mapInfo.forEach(System.out::println);

        assertNotNull(mapInfo);
        assertFalse(mapInfo.isEmpty());
        assertThat(mapInfo.size()).isEqualTo(numOfRows);
    }

    @Test
    void 신호등잔여시간_API_기본요청_테스트() throws JsonProcessingException {
        List<TDataSignalResponse> signalInfo = tDataApiClient.getSignalInfo(TDataRequest.from());

//        signalInfo.forEach(System.out::println);

        assertNotNull(signalInfo);
        assertFalse(signalInfo.isEmpty());
        assertThat(signalInfo.size()).isEqualTo(1000);
    }

    @Test
    void 신호등잔여시간_API_페이지네이션_테스트() throws JsonProcessingException {
        final int pageNo = 1;
        final int numOfRows = 5;
        List<TDataSignalResponse> signalInfo = tDataApiClient.getSignalInfo(TDataRequest.fromPagination(pageNo, numOfRows));

//        signalInfo.forEach(System.out::println);

        assertNotNull(signalInfo);
        assertFalse(signalInfo.isEmpty());
        assertThat(signalInfo.size()).isEqualTo(numOfRows);
    }

    @Test
    void 신호등잔여시간_API_특정교차로_조회_테스트() throws JsonProcessingException {
        final int itstId = 1015;
        List<TDataSignalResponse> signalInfo = tDataApiClient.getSignalInfo(TDataRequest.fromItstId(itstId));

//        signalInfo.forEach(System.out::println);

        assertNotNull(signalInfo);
        assertFalse(signalInfo.isEmpty());
        assertThat(signalInfo.size()).isEqualTo(1);
        assertThat(signalInfo.get(0).itstId()).isEqualTo(itstId);
    }


}