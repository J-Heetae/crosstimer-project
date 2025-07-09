package com.goose.crosstimer.crossroad.service;

import com.goose.crosstimer.api.dto.TDataSignalResponse;
import com.goose.crosstimer.api.service.TDataApiService;
import com.goose.crosstimer.common.exception.CustomException;
import com.goose.crosstimer.common.exception.ErrorCode;
import com.goose.crosstimer.crossroad.domain.Crossroad;
import com.goose.crosstimer.crossroad.dto.CrossroadRangeRequest;
import com.goose.crosstimer.crossroad.dto.CrossroadRangeResponse;
import com.goose.crosstimer.signal.domain.SignalCache;
import com.goose.crosstimer.crossroad.dto.CrossroadWithSignalResponse;
import com.goose.crosstimer.crossroad.repository.CrossroadJpaRepository;
import com.goose.crosstimer.signal.domain.SignalInfo;
import com.goose.crosstimer.signal.dto.SignalCacheResponse;
import com.goose.crosstimer.signal.mapper.SignalCacheMapper;
import com.goose.crosstimer.signal.repository.SignalCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrossroadService {
    private final CrossroadJpaRepository crossroadJpaRepository;
    private final SignalCacheRepository signalCacheRepository;
    private final TDataApiService tDataApiService;
    private final SignalCacheMapper signalCacheMapper;

    public CrossroadWithSignalResponse getCrossroadWithSignals(Integer crossroadId) {
        log.debug("getCrossroadWithSignals 호출: crossroadId={}", crossroadId);
        Crossroad findCrossroad = crossroadJpaRepository.findById(crossroadId)
                .orElseThrow(() -> new CustomException(ErrorCode.CROSSROAD_NOT_FOUND));
        log.debug("찾은 교차로: id={}, name={}", findCrossroad.getCrossroadId(), findCrossroad.getName());

        SignalCache findCache = signalCacheRepository.findById(findCrossroad.getCrossroadId())
                .orElseGet(() -> fetchAndCacheSignal(crossroadId)); //캐시된 신호 데이터가 없는 경우
        log.debug("사용할 SignalCache: crossroadId={}, sendAt={}, cachedAt={}",
                findCache.getCrossroadId(), findCache.getSendAt(), findCache.getCachedAt());


        List<SignalCacheResponse> signalCacheResponses = findCache.getSignals().stream()
                .map(this::toSignalCacheResponse)
                .toList();
        log.debug("매핑된 SignalCacheResponse 개수={}", signalCacheResponses.size());

        return new CrossroadWithSignalResponse(
                findCrossroad.getCrossroadId(),
                findCrossroad.getName(),
                findCrossroad.getLat(),
                findCrossroad.getLng(),
                findCache.getSendAt(),
                findCache.getCachedAt(),
                signalCacheResponses
        );
    }

    private SignalCache fetchAndCacheSignal(Integer crossroadId) {
        //외부 API 호출
        log.debug("fetchAndCacheSignal 호출: crossroadId={}", crossroadId);
        List<TDataSignalResponse> responses = tDataApiService.getSignalsByCrossroadId(crossroadId);

        //가장 최신 데이터만 사용
        if (responses.isEmpty()) {
            log.warn("TData API 응답 없음 or 빈 리스트: crossroadId={}", crossroadId);
            throw new CustomException(ErrorCode.EXTERNAL_SIGNAL_API_ERROR);
        }
        TDataSignalResponse latest = responses.get(0);

        //API 응답을 Redis 캐시
        SignalCache cache = signalCacheMapper.fromTDataResponse(latest);
        signalCacheRepository.save(cache);
        log.debug("SignalCache 저장 완료: crossroadId={}", cache.getCrossroadId());

        return cache;
    }

    private SignalCacheResponse toSignalCacheResponse(SignalInfo info) {
        return new SignalCacheResponse(
                info.getDirection(),
                info.getStatus(),
                info.getRemainingSec()
        );
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
                    crossroad.getCrossroadId(),
                    crossroad.getName(),
                    crossroad.getLat(),
                    crossroad.getLng()
            ));
        }
        return result;
    }
}
