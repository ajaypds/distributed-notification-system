package com.example.notificationdispatcher.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;
    private static final String PROCESSED_KEY = "processed-events";

    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAlreadyProcessed(String eventId) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(PROCESSED_KEY, eventId)
        );
    }

    public void markProcessed(String eventId) {
        redisTemplate.opsForSet().add(PROCESSED_KEY, eventId);
    }
}
