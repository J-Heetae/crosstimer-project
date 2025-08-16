package com.goose.crosstimer.crossroad.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrossroadInitLoad {

    private final CrossroadService crossroadService;

    @PostConstruct
    public void initUpsertOnStartup() {
        log.info("기동 후 1회 Upsert 실행");
        crossroadService.upsertCrossroads();
    }
}
