package com.goose.crosstimer.crossroad.service;

import com.goose.crosstimer.common.exception.CustomException;
import com.goose.crosstimer.common.exception.ErrorCode;
import com.goose.crosstimer.crossroad.domain.Crossroad;
import com.goose.crosstimer.crossroad.dto.CrossroadRangeRequest;
import com.goose.crosstimer.crossroad.dto.CrossroadWithSignalResponse;
import com.goose.crosstimer.crossroad.repository.CrossroadJpaRepository;
import com.goose.crosstimer.signal.domain.SignalCycle;
import com.goose.crosstimer.signal.dto.SignalCycleResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CrossroadService {
    private final CrossroadJpaRepository crossroadJpaRepository;

    public CrossroadWithSignalResponse getCrossroadWithSignalCycles(Integer itstId) {
        Crossroad findCrossroad = crossroadJpaRepository.findCrossroadWithSignalCycles(itstId).orElseThrow(
                () -> new CustomException(ErrorCode.CROSSROAD_NOT_FOUND)
        );

        List<SignalCycleResponse> signalCycleResponseList = getSignalCycleResponses(findCrossroad);

        return new CrossroadWithSignalResponse(
                findCrossroad.getItstId(),
                findCrossroad.getName(),
                findCrossroad.getLat(),
                findCrossroad.getLng(),
                signalCycleResponseList
        );
    }

    private List<SignalCycleResponse> getSignalCycleResponses(Crossroad findCrossroad) {
        List<SignalCycle> signalCycleList = Optional.ofNullable(findCrossroad.getSignalCycleList())
                .orElse(List.of());

        List<SignalCycleResponse> signalCycleResponseList = new ArrayList<>();
        for (SignalCycle signalCycle : signalCycleList) {
            signalCycleResponseList.add(new SignalCycleResponse(
                    signalCycle.getDirection(),
                    signalCycle.getReferenceGreenStart(),
                    signalCycle.getGreenSeconds(),
                    signalCycle.getRedSeconds(),
                    signalCycle.getUpdatedAt().toEpochMilli()
            ));
        }
        return signalCycleResponseList;
    }

    public List<Crossroad> getCrossroadsInRange(CrossroadRangeRequest request) {
        return crossroadJpaRepository.findByLatBetweenAndLngBetween(
                request.swLat(), request.neLat(),
                request.swLng(), request.neLng()
        );
    }
}
