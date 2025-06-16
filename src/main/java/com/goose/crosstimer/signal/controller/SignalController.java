package com.goose.crosstimer.signal.controller;

import com.goose.crosstimer.signal.dto.SignalResponse;
import com.goose.crosstimer.signal.service.SignalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/signal")
@RequiredArgsConstructor
public class SignalController {
    private final SignalService signalService;

    @GetMapping("/{itstId}")
    public ResponseEntity<SignalResponse> getSignal(@PathVariable Integer itstId) {
        SignalResponse signalByItstId = signalService.getSignalByItstId(itstId);
        return ResponseEntity.ok(signalByItstId);
    }
}
