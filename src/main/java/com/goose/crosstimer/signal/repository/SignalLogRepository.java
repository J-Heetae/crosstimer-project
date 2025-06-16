package com.goose.crosstimer.signal.repository;

import com.goose.crosstimer.signal.domain.SignalLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SignalLogRepository extends MongoRepository<SignalLog, String> {
}
