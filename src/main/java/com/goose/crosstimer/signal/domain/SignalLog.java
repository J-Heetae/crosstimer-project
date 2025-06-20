package com.goose.crosstimer.signal.domain;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Document("signal_logs")
public class SignalLog {
    @Id
    private String id;
    private Integer itstId;
    private String direction;
    private Long trsmUtcTime;
    private Instant loggedAt;
    private Integer remainingDeciSeconds;
    private String status;
}
