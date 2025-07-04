package com.goose.crosstimer.crossroad.service;

import com.goose.crosstimer.common.exception.CustomException;
import com.goose.crosstimer.common.exception.ErrorCode;
import com.goose.crosstimer.crossroad.domain.Crossroad;
import com.goose.crosstimer.crossroad.dto.CrossroadRangeRequest;
import com.goose.crosstimer.crossroad.dto.CrossroadRangeResponse;
import com.goose.crosstimer.crossroad.dto.CrossroadWithSignalResponse;
import com.goose.crosstimer.crossroad.repository.CrossroadJpaRepository;
import com.goose.crosstimer.signal.domain.SignalCache;
import com.goose.crosstimer.signal.domain.SignalCycle;
import com.goose.crosstimer.signal.dto.SignalCacheResponse;
import com.goose.crosstimer.signal.dto.SignalCycleResponse;
import com.goose.crosstimer.signal.service.SignalCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrossroadService {
    private final CrossroadJpaRepository crossroadJpaRepository;
    private final SignalCacheService signalCacheService;

    /**
     * 교차로와 해당 교차로의 SignalCycle 및 최신 SignalCache 정보를 조회
     *
     * @param itstId 교차로 ID
     * @return 교차로 정보 및 신호 주기/캐시 정보
     * @throws CustomException 교차로 미존재 시 오류 발생
     */
    public CrossroadWithSignalResponse getCrossroadWithSignalCycles(Integer itstId) {
        Crossroad findCrossroad = crossroadJpaRepository.findCrossroadWithSignalCycles(itstId).orElseThrow(
                () -> new CustomException(ErrorCode.CROSSROAD_NOT_FOUND)
        );

        List<SignalCycleResponse> signalCycleResponseList = getSignalCycleResponses(findCrossroad);

        List<SignalCacheResponse> signalCacheResponseList = new ArrayList<>();
        for (SignalCycleResponse signalCycleResponse : signalCycleResponseList) {
            SignalCache cache = signalCacheService.findCache(itstId, signalCycleResponse.direction());
            if (cache == null) {
                continue;
            }

            String cacheId = cache.getId();
            String direction = cacheId.split(":")[1];

            signalCacheResponseList.add(new SignalCacheResponse(
                    cache.getSignalTimestamp(),
                    direction,
                    cache.getStatus(),
                    cache.getRemaining(),
                    cache.getPredictedGreenSec(),
                    cache.getPredictedRedSec()
            ));
        }

        return new CrossroadWithSignalResponse(
                findCrossroad.getItstId(),
                findCrossroad.getName(),
                findCrossroad.getLat(),
                findCrossroad.getLng(),
                signalCacheResponseList
        );
    }


    /**
     * 특정 교차로의 SignalCycle을 SignalCycleResponse로 변환
     *
     * @param findCrossroad 교차로
     * @return SignalCycleResponse 리스트
     */
    private List<SignalCycleResponse> getSignalCycleResponses(Crossroad findCrossroad) {
        List<SignalCycle> signalCycleList = Optional.ofNullable(findCrossroad.getSignalCycleList())
                .orElse(List.of());

        List<SignalCycleResponse> signalCycleResponseList = new ArrayList<>();
        for (SignalCycle signalCycle : signalCycleList) {
            signalCycleResponseList.add(new SignalCycleResponse(
                    signalCycle.getDirection(),
                    signalCycle.getGreenSec(),
                    signalCycle.getRedSec(),
                    signalCycle.getUpdatedAt().toEpochMilli()
            ));
        }
        return signalCycleResponseList;
    }

    /**
     * 범위 내 위/경도 조건으로 교차로 리스트를 조회
     *
     * @param request 범위 정보(SW/NE 좌표)
     * @return 해당 범위 교차로 DTO 리스트
     * @throws CustomException 조회 결과가 없으면 오류 발생
     */
    public List<CrossroadRangeResponse> getCrossroadsInRange(CrossroadRangeRequest request) {
        List<Crossroad> crossroadList = crossroadJpaRepository.findByLatBetweenAndLngBetween(
                request.swLat(), request.neLat(),
                request.swLng(), request.neLng()
        );

        if (crossroadList.isEmpty()) { //교차로가 존재하지 않을 경우
            throw new CustomException(ErrorCode.CROSSROAD_RANGE_EMPTY);
        }

        List<CrossroadRangeResponse> result = new ArrayList<>();
        for (Crossroad crossroad : crossroadList) {
            result.add(new CrossroadRangeResponse(
                    crossroad.getItstId(),
                    crossroad.getName(),
                    crossroad.getLat(),
                    crossroad.getLng()
            ));
        }
        return result;
    }
}
