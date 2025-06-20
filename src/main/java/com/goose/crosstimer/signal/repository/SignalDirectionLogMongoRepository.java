package com.goose.crosstimer.signal.repository;

import com.goose.crosstimer.signal.domain.SignalDirectionLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SignalDirectionLogMongoRepository extends MongoRepository<SignalDirectionLog, String> {
}
