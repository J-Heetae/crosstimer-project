package com.goose.crosstimer.crossroad.service;

import com.goose.crosstimer.api.dto.TDataCrossroadResponse;
import com.goose.crosstimer.api.service.TDataApiService;
import com.goose.crosstimer.crossroad.domain.Crossroad;
import com.goose.crosstimer.crossroad.mapper.CrossroadMapper;
import com.goose.crosstimer.crossroad.repository.CrossroadJpaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrossroadBatchService {
    private final TDataApiService tDataApiService;
    private final CrossroadJpaRepository crossroadJpaRepository;

    /**
     * TData 교차로 MAP 정보 API를 호출하여 제공된 모든 교차로 정보를 MySQL에 Upsert 처리
     */
//    @Scheduled(fixedRate = 999_999_999L)
    @Transactional
    public void upsertCrossroads() {
        log.info("교차로 데이터 Upsert");

        List<TDataCrossroadResponse> fetched = new ArrayList<>();
        int pageNo = 1;
        while (true) {
            List<TDataCrossroadResponse> responses = tDataApiService.getCrossroadsMaxRow(pageNo++);
            if (responses.isEmpty()) { //더 이상 조회 안될때까지 반복
                break;
            }
            fetched.addAll(responses);
        }

        int saveCount = 0;
        int updateCount = 0;
        for (TDataCrossroadResponse response : fetched) {
            Optional<Crossroad> findCrossroad = crossroadJpaRepository.findById(response.crossroadId());

            if (findCrossroad.isEmpty()) { //신규 교차로인 경우 insert
                crossroadJpaRepository.save(CrossroadMapper.fromDto(response));
                saveCount++;
            } else { // 기존 교차로인 경우 update
                findCrossroad.get().update(
                        response.name(),
                        response.lat(),
                        response.lng()
                );
                updateCount++;
            }
        }
        log.info("교차로 데이터 Upsert 완료 : save {}, update {}", saveCount, updateCount);
    }
}
