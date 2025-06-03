package com.goose.crosstimer.repository;

import com.goose.crosstimer.domain.SignalInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SignalInfoRepository extends JpaRepository<SignalInfo, Integer> {
}
