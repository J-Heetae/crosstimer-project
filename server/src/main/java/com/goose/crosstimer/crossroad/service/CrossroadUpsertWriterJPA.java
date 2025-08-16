package com.goose.crosstimer.crossroad.service;

import com.goose.crosstimer.api.dto.TDataCrossroadResponse;
import com.goose.crosstimer.crossroad.domain.Crossroad;
import com.goose.crosstimer.crossroad.mapper.CrossroadMapper;
import com.goose.crosstimer.crossroad.repository.CrossroadJpaRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CrossroadUpsertWriterJPA {
    private final EntityManager em;
    private final CrossroadJpaRepository crossroadJpaRepository;

    private static final int BATCH_SIZE = 500;

    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 60)
    public UpsertResult write(List<TDataCrossroadResponse> page) {
        if (page == null || page.isEmpty()) {
            return new UpsertResult(0, 0);
        }

        Set<Integer> ids = page.stream()
                .map(TDataCrossroadResponse::crossroadId)
                .collect(Collectors.toSet());

        Map<Integer, Crossroad> existingMap = crossroadJpaRepository.findAllById(ids)
                .stream()
                .collect(Collectors.toMap(Crossroad::getCrossroadId, it -> it));


        List<Crossroad> toInsert = new ArrayList<>();
        int updated = 0;

        for (TDataCrossroadResponse dto : page) {
            Crossroad exist = existingMap.get(dto.crossroadId());
            if (exist != null) {
                exist.update(dto.name(), dto.lat(), dto.lng());
                updated++;
            } else {
                toInsert.add(CrossroadMapper.fromDto(dto));
            }

            // 너무 큰 페이지면 중간중간 flush/clear
            if ((updated + toInsert.size()) % BATCH_SIZE == 0) {
                flushAndClear();
            }
        }

        if (!toInsert.isEmpty()) {
            crossroadJpaRepository.saveAll(toInsert);
        }

        flushAndClear();
        int inserted = toInsert.size();
        return new UpsertResult(inserted, updated);
    }

    private void flushAndClear() {
        em.flush();
        em.clear();
    }

    public record UpsertResult(int inserted, int updated) {
    }
}
