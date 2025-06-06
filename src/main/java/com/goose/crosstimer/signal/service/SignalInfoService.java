package com.goose.crosstimer.signal.service;

import com.goose.crosstimer.signal.domain.SignalInfo;
import com.goose.crosstimer.signal.dto.SignalInfoResponse;
import com.goose.crosstimer.signal.repository.SignalInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class SignalInfoService {
    private final SignalInfoRepository signalInfoRepository;

    public SignalInfoResponse getSignalInfo(Integer itstId) {
        SignalInfo findSignalInfo = signalInfoRepository.findById(itstId)
                .orElseThrow(() -> new NoSuchElementException("해당하는 교차로가 없습니다. itstId=" + itstId));
        return SignalInfoResponse.from(findSignalInfo);
    }
}
