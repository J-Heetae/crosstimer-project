package com.goose.crosstimer.crossroad.controller;

import com.goose.crosstimer.crossroad.dto.CrossroadRangeRequest;
import com.goose.crosstimer.crossroad.dto.CrossroadRangeResponse;
import com.goose.crosstimer.crossroad.dto.CrossroadWithSignalResponse;
import com.goose.crosstimer.crossroad.service.CrossroadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/crossroads")
@RequiredArgsConstructor
public class CrossroadController {
    private final CrossroadService crossroadService;

    /**
     * 캐시된 각 방향의 신호 정보를 포함한 교차로 검색
     * @param itstId 교차로 ID
     * @return 캐시된 각 방향의 신호 정보를 포함한 교차로 정보
     */
    @GetMapping("/{itstId}/signal-cache")
    public ResponseEntity<CrossroadWithSignalResponse> getCrossroadWithSignalCache(@PathVariable(name = "itstId") Integer itstId) {
        return ResponseEntity.ok(crossroadService.getCrossroadWithSignalCycles(itstId));
    }

    /**
     * 남서쪽 좌표와 북동쪽 좌표 범위 내의 교차로 리스트 검색
     * @param request 남서쪽, 북동쪽의 위도 경도
     * @return 범위 내의 교차로 리스트
     */
    @PostMapping
    public ResponseEntity<List<CrossroadRangeResponse>> getCrossroadInRange(@RequestBody CrossroadRangeRequest request) {
        return ResponseEntity.ok(crossroadService.getCrossroadsInRange(request));
    }
}
