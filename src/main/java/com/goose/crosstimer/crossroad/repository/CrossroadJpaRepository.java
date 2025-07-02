package com.goose.crosstimer.crossroad.repository;

import com.goose.crosstimer.crossroad.domain.Crossroad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CrossroadJpaRepository extends JpaRepository<Crossroad, Integer> {

    /**
     * 남서, 북동쪽 좌표 범위 내의 교차로 리스트 조회
     * @param swLat 남서쪽 위도
     * @param neLat 북동쪽 위도
     * @param swLng 남서쪽 경도
     * @param neLng 북서쪽 경도
     * @return 범위 내의 교차로 리스트
     */
    List<Crossroad> findByLatBetweenAndLngBetween(
            Double swLat, Double neLat,
            Double swLng, Double neLng
    );

    /**
     * SignalCycle 리스트를 포함한 교차로 조회
     * @param itstId 교차로 ID
     * @return SignalCycle 리스트를 포함한 교차로
     */
    @Query("SELECT c FROM Crossroad c LEFT JOIN FETCH c.signalCycleList WHERE c.itstId = :itstId")
    Optional<Crossroad> findCrossroadWithSignalCycles(@Param("itstId")Integer itstId);
}
