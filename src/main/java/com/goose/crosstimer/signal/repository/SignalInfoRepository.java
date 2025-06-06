package com.goose.crosstimer.signal.repository;

import com.goose.crosstimer.signal.domain.SignalInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignalInfoRepository extends JpaRepository<SignalInfo, Integer> {
}
