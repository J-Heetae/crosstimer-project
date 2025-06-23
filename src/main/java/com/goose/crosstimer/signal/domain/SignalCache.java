package com.goose.crosstimer.signal.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalCache {
    private Instant signalTimestamp;
    private String status;
    private Integer remaining;
    private Integer predictedGreenSec;
    private Integer predictedRedSec;
    private Instant updatedAt;
}
