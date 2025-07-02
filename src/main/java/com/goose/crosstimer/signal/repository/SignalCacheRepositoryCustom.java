package com.goose.crosstimer.signal.repository;

import com.goose.crosstimer.signal.domain.SignalCache;

import java.util.List;

public interface SignalCacheRepositoryCustom {
    void saveAllPipeline(List<SignalCache> caches);

    List<SignalCache> findByItstId(Long itstId);
}
