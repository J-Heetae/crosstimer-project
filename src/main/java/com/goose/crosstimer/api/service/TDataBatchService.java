package com.goose.crosstimer.api.service;

import com.goose.crosstimer.api.client.TDataApiClient;
import com.goose.crosstimer.api.dto.CrossroadResponseDto;
import com.goose.crosstimer.api.dto.SignalResponseDto;
import com.goose.crosstimer.api.dto.TDataApiRequestDto;
import com.goose.crosstimer.domain.Crossroad;
import com.goose.crosstimer.domain.SignalInfo;
import com.goose.crosstimer.mapper.CrossroadMapper;
import com.goose.crosstimer.mapper.SignalInfoMapper;
import com.goose.crosstimer.repository.CrossroadRepository;
import com.goose.crosstimer.repository.SignalInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TDataBatchService {
    private final TDataApiClient client;
    private final CrossroadRepository crossroadRepository;
    private final SignalInfoRepository signalInfoRepository;

    @Scheduled(fixedRate = 300000L)
    public void fetchCrossroadData() {
        int pageNo = 1;
        final int numOfRows = 1000;
        List<CrossroadResponseDto> crossroadInfoList = new ArrayList<>();

        while (true) {
            List<CrossroadResponseDto> temp = client.getCrossroadInfo(
                    TDataApiRequestDto.fromPagination(pageNo, numOfRows)
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

    @Scheduled(fixedRate = 300000L)
    public void fetchSignalInfo() {
        final int numOfRows = 1000;
        List<SignalResponseDto> signalResponseDtoList = new ArrayList<>();
        for (int pageNo = 1; pageNo <= 10; pageNo++) {
            signalResponseDtoList.addAll(client.getSignalInfo(TDataApiRequestDto.fromPagination(pageNo, numOfRows)));
        }
        List<SignalInfo> signalInfoList = signalResponseDtoList.stream()
                .map(SignalInfoMapper::fromDto)
                .toList();

        signalInfoRepository.saveAll(signalInfoList);
    }
}
