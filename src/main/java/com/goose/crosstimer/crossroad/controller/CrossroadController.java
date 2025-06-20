package com.goose.crosstimer.crossroad.controller;

import com.goose.crosstimer.crossroad.domain.Crossroad;
import com.goose.crosstimer.crossroad.dto.CrossroadRangeRequest;
import com.goose.crosstimer.crossroad.service.CrossroadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/crossroad")
@RequiredArgsConstructor
public class CrossroadController {
    private final CrossroadService crossroadService;

    @GetMapping("/{itstId}/signal-cycles")
    public ResponseEntity<CrossroadWithSignalResponse> getCrossroadWithSignalCycle(@PathVariable(name = "itstId") Integer itstId) {
        return ResponseEntity.ok(crossroadService.getCrossroadWithSignalCycles(itstId));
    }

    @PostMapping
    public ResponseEntity<List<Crossroad>> getCrossroadInRange(@RequestBody CrossroadRangeRequest request) {
        return ResponseEntity.ok(crossroadService.getCrossroadsInRange(request));
    }
}
