package com.goose.crosstimer.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.goose.crosstimer.signal.dto.SignalCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, SignalCache> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, SignalCache> redisTemplate = new RedisTemplate<>();

        //Redis 연결 팩토리 주입
        redisTemplate.setConnectionFactory(factory);

        //커스텀 ObjectMapper
        //JavaTimeModule: Instant 등 Java8 날짜 타입 지원
        //WRITE_DATES_AS_TIMESTAMPS 비활성화: ISO-8601 문자열 직렬화
        ObjectMapper customObjectMapper = Jackson2ObjectMapperBuilder.json()
                .modules(new JavaTimeModule())
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();

        Jackson2JsonRedisSerializer<SignalCache> serializer =
                new Jackson2JsonRedisSerializer<>(customObjectMapper, SignalCache.class);

        //Key/Value serializer 설정
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(serializer);

        //초기화
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
