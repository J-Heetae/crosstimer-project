package com.goose.crosstimer.signal.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import java.time.Instant;

@RedisHash(value = "signal")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalCache {
    @Id
    private String id;
    private Instant signalTimestamp;
    private String status;
    private Integer remaining;
    private Integer predictedGreenSec;
    private Integer predictedRedSec;
    private Instant updatedAt;
}
