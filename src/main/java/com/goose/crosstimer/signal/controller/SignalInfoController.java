package com.goose.crosstimer.signal.controller;

import com.goose.crosstimer.signal.dto.SignalInfoResponse;
import com.goose.crosstimer.signal.service.SignalInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/signal")
@RequiredArgsConstructor
public class SignalInfoController {
    private final SignalInfoService signalInfoService;

    @GetMapping("/{itstId}")
    public ResponseEntity<SignalInfoResponse> getSignalInfo(@PathVariable int itstId) {
        return ResponseEntity.ok(signalInfoService.getSignalInfo(itstId));
    }
}
