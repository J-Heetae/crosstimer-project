package com.goose.crosstimer.signal.service;

import com.goose.crosstimer.signal.domain.SignalLog;
import com.goose.crosstimer.signal.repository.SignalLogMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SignalLogService {
    private final SignalLogMongoRepository logMongoRepository;

    public void saveLogs(List<SignalLog> logs) {
        logMongoRepository.saveAll(logs);
    }
}
