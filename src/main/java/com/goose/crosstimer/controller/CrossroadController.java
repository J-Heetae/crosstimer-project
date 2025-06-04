package com.goose.crosstimer.controller;

import com.goose.crosstimer.domain.Crossroad;
import com.goose.crosstimer.domain.CrossroadRangeRequestDto;
import com.goose.crosstimer.service.CrossroadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/crossroad")
@RequiredArgsConstructor
public class CrossroadController {
    private final CrossroadService crossroadService;

    @PostMapping
    public ResponseEntity<List<Crossroad>> getCrossroadInRange(@RequestBody CrossroadRangeRequestDto requestDto) {
        return ResponseEntity.ok(crossroadService.getCrossroadsInRange(requestDto));
    }
}
