package com.goose.crosstimer.signal.service;

import com.goose.crosstimer.signal.domain.SignalLog;
import com.goose.crosstimer.signal.repository.SignalLogMongoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SignalLogService {
    private final SignalLogMongoRepository logMongoRepository;

    /**
     * 전달된 SignalLog 리스트 MongoDB에 저장
     * <p>
     * @param logs 저장할 SignalLog 리스트
     * @throws IllegalArgumentException logs가 null이거나 비어있으면 예외 발생
     */
    @Transactional
    public void saveLogs(List<SignalLog> logs) {
        if (logs == null || logs.isEmpty()) {
            throw new IllegalArgumentException("SignalLog 리스트가 null이거나 비어있습니다.");
        }

        log.info("SignalLog 저장 시작: 총 {}건", logs.size());
        logMongoRepository.saveAll(logs);
        log.info("로그 MongoDB에 저장 완료");
    }
}
