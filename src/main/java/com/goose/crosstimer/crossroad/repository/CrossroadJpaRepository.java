package com.goose.crosstimer.crossroad.repository;

import com.goose.crosstimer.crossroad.domain.Crossroad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrossroadJpaRepository extends JpaRepository<Crossroad, Integer> {

    /**
     * 남서, 북동쪽 좌표 범위 내의 교차로 리스트 조회
     *
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
}
