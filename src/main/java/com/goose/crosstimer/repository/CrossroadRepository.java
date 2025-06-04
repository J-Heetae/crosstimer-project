package com.goose.crosstimer.repository;

import com.goose.crosstimer.domain.Crossroad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CrossroadRepository extends JpaRepository<Crossroad, Integer> {

    List<Crossroad> findByMapCtptIntLatBetweenAndMapCtptIntLotBetween(
            Double swLat, Double neLat,
            Double swLot, Double neLot
    );
}
