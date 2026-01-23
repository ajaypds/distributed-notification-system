package com.example.notificationdispatcher.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;

    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isDuplicate(String eventId) {
        Long added = redisTemplate.opsForSet()
                .add("processed-events", eventId);
        return added == 0;
    }
}
