package com.goose.crosstimer.crossroad.repository;

import com.goose.crosstimer.crossroad.domain.Crossroad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CrossroadJpaRepository extends JpaRepository<Crossroad, Integer> {
    List<Crossroad> findByLatBetweenAndLngBetween(
            Double swLat, Double neLat,
            Double swLng, Double neLng
    );

    @Query("SELECT c FROM Crossroad c LEFT JOIN FETCH c.signalCycleList WHERE c.itstId = :itstId")
    Optional<Crossroad> findCrossroadWithSignalCycles(@Param("itstId")Integer itstId);
}
