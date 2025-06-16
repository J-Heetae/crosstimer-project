package com.goose.crosstimer.signal.domain;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Document("signal_logs")
public class SignalLog {
    @Id
    private String id;
    private Integer itstId;
    private Long trsmUtcTime;
    private Instant loggedAt;
    private Map<String, SignalData> signals;


    @Getter
    @AllArgsConstructor
    public static class SignalData {
        private Integer remainingDeciSeconds;
        private String status;
    }
}
