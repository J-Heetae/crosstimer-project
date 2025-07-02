package com.goose.crosstimer.signal.repository;

import com.goose.crosstimer.signal.domain.SignalCycle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface SignalCycleJpaRepository extends JpaRepository<SignalCycle, Long> {

    /**
     * 모든 SignalCycle 조회 (교차로 포함)
     * @return 모든 SignalCycle 리스트
     */
    @Query("SELECT sc FROM SignalCycle sc JOIN FETCH sc.crossroad")
    List<SignalCycle> findAllWithCrossroad();
}
