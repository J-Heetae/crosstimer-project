package com.goose.crosstimer.api.service;

import com.goose.crosstimer.api.client.TDataApiClient;
import com.goose.crosstimer.api.dto.TDataCrossroadResponse;
import com.goose.crosstimer.api.dto.TDataSignalResponse;
import com.goose.crosstimer.api.dto.TDataRequest;
import com.goose.crosstimer.crossroad.domain.Crossroad;
import com.goose.crosstimer.signal.domain.SignalInfo;
import com.goose.crosstimer.crossroad.mapper.CrossroadMapper;
import com.goose.crosstimer.signal.domain.SignalLog;
import com.goose.crosstimer.signal.mapper.SignalInfoMapper;
import com.goose.crosstimer.crossroad.repository.CrossroadRepository;
import com.goose.crosstimer.signal.mapper.SignalLogMapper;
import com.goose.crosstimer.signal.repository.SignalInfoRepository;
import com.goose.crosstimer.signal.repository.SignalLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TDataBatchService {
    private final TDataApiClient client;

    private final CrossroadJpaRepository crossroadJpaRepository;
    private final SignalDirectionLogMongoRepository signalDirectionLogMongoRepository;

//    @Scheduled(fixedRate = 300_000L)
    public void fetchCrossroadData() {
        int pageNo = 1;
        final int numOfRows = 1000;
        List<TDataCrossroadResponse> crossroadInfoList = new ArrayList<>();

        while (true) {
            List<TDataCrossroadResponse> temp = client.getCrossroadInfo(
                    TDataRequest.fromPagination(pageNo, numOfRows)
            );

            if (temp.isEmpty()) {
                break;
            }

            crossroadInfoList.addAll(temp);

            pageNo++;
        }
        List<Crossroad> crossroadList = crossroadInfoList.stream()
                .map(CrossroadMapper::fromDto)
                .toList();

        crossroadRepository.saveAll(crossroadList);
    }

    //    @PostConstruct
    @Transactional
    public void getSignalLog() {
        Set<Integer> crossroadSet = new HashSet<>();

        final int MAX_CALLS = 500;
        for (int call = 1; call <= MAX_CALLS; call++) {
            List<SignalDirectionLog> saveLogList = new ArrayList<>();

            List<TDataSignalResponse> responseList;
            try {
                Thread.sleep(1000);
                responseList = client.getSignalInfo(TDataRequest.fromPagination(call, 1000));
                if (responseList == null) {
                    log.warn("client.getSignalInfo returned null for page {}", call);
                    break;
                }
            } catch (Exception e) {
                log.error("TData API 호출 실패(page {})", call, e);
                break;
            }

            if (responseList.isEmpty()) {
                break;
            }

            for (TDataSignalResponse response : responseList) {
                crossroadSet.add(response.itstId());

                Map<String, SignalData> directionMap = getStringSignalDataMap(response);

                List<String> exceptionStatus = List.of("dark", "null");
                List<Integer> exceptionSeconds = List.of(36001);

                for (String direction : directionMap.keySet()) {
                    SignalData currDirection = directionMap.get(direction);

                    if (currDirection.status() == null || currDirection.remainingDeciSeconds() == null) {
                        continue;
                    }

                    if (exceptionStatus.contains(currDirection.status()) ||
                            exceptionSeconds.contains(currDirection.remainingDeciSeconds())) {
                        continue;
                    }

        signalLogRepository.saveAll(signalLogList);
    }
}
