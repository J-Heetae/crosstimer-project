package com.goose.crosstimer.crossroad.controller;

import com.goose.crosstimer.crossroad.dto.CrossroadRangeRequest;
import com.goose.crosstimer.crossroad.dto.CrossroadRangeResponse;
import com.goose.crosstimer.crossroad.dto.CrossroadWithSignalResponse;
import com.goose.crosstimer.crossroad.service.CrossroadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/crossroads")
@RequiredArgsConstructor
public class CrossroadController {
    private final CrossroadService crossroadService;

    /**
     * 캐시된 각 방향의 신호 정보를 포함한 교차로 검색
     * @param crossroadId 교차로 ID
     * @return 캐시된 각 방향의 신호 정보를 포함한 교차로 정보
     */
    @GetMapping("/{crossroadId}/signals")
    public ResponseEntity<CrossroadWithSignalResponse> getCrossroadWithSignals(@PathVariable(name = "crossroadId") Integer crossroadId) {
        return ResponseEntity.ok(crossroadService.getCrossroadWithSignals(crossroadId));
    }

    /**
     * 남서쪽(SW) 및 북동쪽(NE) 좌표 범위 내의 교차로를 슬라이스(Slice) 방식으로 페이징 조회합니다.
     *
     * @param request 남서쪽(SW) 및 북동쪽(NE) 좌표를 담은 요청 객체
     * @param page    조회할 페이지 번호 (1~45, 기본값: 1)
     * @param size    페이지당 결과 개수 (1~15, 기본값: 15)
     * @return 슬라이스 형식의 교차로 DTO 목록
     */
    @PostMapping
    public ResponseEntity<Slice<CrossroadRangeResponse>> getCrossroadsInRange(@RequestBody CrossroadRangeRequest request,
                                                                              @RequestParam(name = "page", defaultValue = "1") int page,
                                                                              @RequestParam(name = "size", defaultValue = "15") int size) {
        log.info(request.toString());
        Slice<CrossroadRangeResponse> crossroadsInRangeWithPaging = crossroadService.getCrossroadsInRangeWithPaging(request, page, size);
        log.info(crossroadsInRangeWithPaging.getContent().toString());
        return ResponseEntity.ok(crossroadsInRangeWithPaging);
    }
}
