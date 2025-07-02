package com.goose.crosstimer.crossroad.service;

import com.goose.crosstimer.api.dto.TDataCrossroadResponse;
import com.goose.crosstimer.api.service.TDataApiService;
import com.goose.crosstimer.crossroad.mapper.CrossroadMapper;
import com.goose.crosstimer.crossroad.repository.CrossroadJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrossroadBatchService {
    private final TDataApiService tDataApiService;
    private final CrossroadJpaRepository crossroadJpaRepository;

    //    @Scheduled(fixedRate = 3_000_000L)
    public void fetchCrossroadData() {
        log.info("교차로 데이터 추가");
        List<TDataCrossroadResponse> crossroadInfoList = new ArrayList<>();

        int pageNo = 1;
        while (true) {
            List<TDataCrossroadResponse> temp = tDataApiService.getCrossroadsMaxRow(pageNo);
            pageNo++;

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
