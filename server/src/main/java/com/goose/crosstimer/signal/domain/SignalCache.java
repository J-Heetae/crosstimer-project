package com.goose.crosstimer.signal.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.time.Instant;
import java.util.List;

@RedisHash("signal")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalCache {
    @Id
    private Integer crossroadId;
    private List<SignalInfo> signals;
    private Instant sendAt;
    private Instant cachedAt;
    @TimeToLive
    private long ttlSeconds;
}
