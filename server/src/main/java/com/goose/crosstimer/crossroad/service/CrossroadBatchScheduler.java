package com.goose.crosstimer.crossroad.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CrossroadBatchScheduler {
    private final CrossroadService crossroadService;

    @Scheduled(cron = "0 0 4 1 * ?", zone = "Asia/Seoul")
    public void upsertCrossroadsMonthly() {
        crossroadService.upsertCrossroads();
    }
}
