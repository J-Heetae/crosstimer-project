package com.goose.crosstimer.crossroad.repository;

import com.goose.crosstimer.crossroad.domain.Crossroad;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrossroadJpaRepository extends JpaRepository<Crossroad, Integer> {

    /**
     * 남서쪽(SW) 및 북동쪽(NE) 좌표 범위 내의 교차로를 페이징 조회합니다.
     *
     * @param swLat    남서쪽(SW) 위도
     * @param neLat    북동쪽(NE) 위도
     * @param swLng    남서쪽(SW) 경도
     * @param neLng    북동쪽(NE) 경도
     * @param pageable 페이징 및 정렬 정보
     * @return 페이징된 교차로 목록
     */
    Slice<Crossroad> findByLatBetweenAndLngBetween(
            Double swLat, Double neLat,
            Double swLng, Double neLng,
            Pageable pageable
    );
}
