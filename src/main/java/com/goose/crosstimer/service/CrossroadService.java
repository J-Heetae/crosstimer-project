package com.goose.crosstimer.service;

import com.goose.crosstimer.domain.Crossroad;
import com.goose.crosstimer.domain.CrossroadRangeRequestDto;
import com.goose.crosstimer.repository.CrossroadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CrossroadService {
    private final CrossroadRepository crossroadRepository;

    public List<Crossroad> getCrossroadsInRange(CrossroadRangeRequestDto requestDto) {
        return crossroadRepository.findByLatBetweenAndLngBetween(
                requestDto.swLat(), requestDto.neLat(),
                requestDto.swLot(), requestDto.neLot()
        );
    }
}
