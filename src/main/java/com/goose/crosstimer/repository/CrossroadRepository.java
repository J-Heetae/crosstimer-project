package com.goose.crosstimer.repository;

import com.goose.crosstimer.domain.Crossroad;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CrossroadRepository extends JpaRepository<Crossroad, Integer> {
}
