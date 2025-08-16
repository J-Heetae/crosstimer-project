package com.goose.crosstimer.api.service;

import com.goose.crosstimer.api.client.TDataApiClient;
import com.goose.crosstimer.api.dto.TDataCrossroadResponse;
import com.goose.crosstimer.api.dto.TDataRequest;
import com.goose.crosstimer.api.dto.TDataSignalResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TDataApiService {
    private final TDataApiClient apiClient;

    public List<TDataCrossroadResponse> getCrossroadsMaxRow(int pageNo) {
        final int numOfRows = 1000;
        return apiClient.getCrossroadInfo(TDataRequest.fromPagination(pageNo, numOfRows));
    }

    public List<TDataSignalResponse> getSignalsByCrossroadId(int crossroadId) {
        return apiClient.getSignalInfo(TDataRequest.fromCrossroadId(crossroadId));
    }
}
