package com.goose.crosstimer.crossroad.service;

import com.goose.crosstimer.crossroad.domain.Crossroad;
import com.goose.crosstimer.crossroad.dto.CrossroadRangeRequest;
import com.goose.crosstimer.crossroad.repository.CrossroadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CrossroadService {
    private final CrossroadRepository crossroadRepository;

    public List<Crossroad> getCrossroadsInRange(CrossroadRangeRequest request) {
        return crossroadRepository.findByLatBetweenAndLngBetween(
                request.swLat(), request.neLat(),
                request.swLng(), request.neLng()
        );
    }
}
