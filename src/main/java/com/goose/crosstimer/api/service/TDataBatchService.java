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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TDataBatchService {
    private final TDataApiClient client;
    private final CrossroadRepository crossroadRepository;
    private final SignalInfoRepository signalInfoRepository;

    @Scheduled(fixedRate = 300000L)
    public void fetchCrossroadData() {
//        List<Crossroad> crossroadList = new ArrayList<>();
//        int pageNo = 1;
//        final int numOfRows = 1000;
//
//        while (true) {
//            List<CrossroadResponseDto> crossroadInfoList = client.getCrossroadInfo(
//                    TDataApiRequestDto.fromPagination(pageNo, numOfRows)
//            );
//
//            if (crossroadInfoList.isEmpty()) {
//                break;
//            }
//
//            List<Crossroad> mappedList = crossroadInfoList.stream()
//                    .map(CrossroadMapper::fromDto)
//                    .toList();
//
//            crossroadList.addAll(mappedList);
//
//            pageNo++;
//        }
//
//        List<Integer> itstIdList = crossroadList.stream().map(Crossroad::getItstId).toList();

        List<Integer> itstIdList = new ArrayList<>();
        for(int i=1; i<=2710; i++) {
            itstIdList.add(i);
        }
        List<Mono<Optional<SignalResponseDto>>> monoList = itstIdList.stream().map(client::getFirstSignalByItstIdAsync).toList();

        List<SignalResponseDto> signalList = Flux.fromIterable(monoList)
                .flatMap(mono -> mono, 1)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collectList()
                .block();

        List<SignalInfo> signalInfoList = Objects.requireNonNull(signalList).stream()
                .map(SignalInfoMapper::fromDto)
                .toList();

        signalInfoRepository.saveAll(signalInfoList);
//        crossroadRepository.saveAll(crossroadList);
    }

}
