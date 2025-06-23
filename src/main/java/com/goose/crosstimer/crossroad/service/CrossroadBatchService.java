package com.goose.crosstimer.crossroad.service;

import com.goose.crosstimer.api.client.TDataApiClient;
import com.goose.crosstimer.api.dto.TDataCrossroadResponse;
import com.goose.crosstimer.api.dto.TDataRequest;
import com.goose.crosstimer.crossroad.mapper.CrossroadMapper;
import com.goose.crosstimer.crossroad.repository.CrossroadJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrossroadBatchService {
    private final TDataApiClient client;
    private final CrossroadJpaRepository crossroadJpaRepository;

//    @Scheduled(fixedRate = 3_000_000L)
    public void fetchCrossroadData() {
        log.info("교차로 데이터 추가");
        List<TDataCrossroadResponse> crossroadInfoList = new ArrayList<>();

        int pageNo = 1;
        final int numOfRows = 1000;
        while (true) {
            List<TDataCrossroadResponse> temp = client.getCrossroadInfo(
                    TDataRequest.fromPagination(pageNo++, numOfRows)
            );

            if (temp.isEmpty()) { //더 이상 조회 안될때까지 반복
                break;
            }
            crossroadInfoList.addAll(temp);
        }
        crossroadJpaRepository.saveAll(crossroadInfoList.stream()
                .map(CrossroadMapper::fromDto)
                .toList());
        log.info("추가된 교차로 데이터 개수 = {}", crossroadInfoList.size());
    }
}
