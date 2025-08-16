package com.goose.crosstimer.crossroad.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CrossroadInitLoad {
    private final CrossroadService crossroadService;

    @PostConstruct
    public void initUpsertOnStartup() {
        crossroadService.upsertCrossroads();
    }
}
