package com.goose.crosstimer.signal.domain;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalInfo {
    private String direction;
    private String status;
    private Integer remainingSec;
}