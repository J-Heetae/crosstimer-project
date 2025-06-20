package com.goose.crosstimer.crossroad.repository;

import com.goose.crosstimer.crossroad.domain.Crossroad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrossroadJpaRepository extends JpaRepository<Crossroad, Integer> {
    List<Crossroad> findByLatBetweenAndLngBetween(
            Double swLat, Double neLat,
            Double swLng, Double neLng
    );
}
