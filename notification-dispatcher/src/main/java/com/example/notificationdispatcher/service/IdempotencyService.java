package com.example.notificationdispatcher.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class IdempotencyService {

    private final StringRedisTemplate redisTemplate;
    private static final String PROCESSED_KEY = "processed-events";
    private static final String PROCESSED_EMAIL_KEY = "processed-email-events";
    private static final String PROCESSED_SMS_KEY = "processed-sms-events";
    private static final String PROCESSED_PUSH_KEY = "processed-push-events";

    public IdempotencyService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public boolean isAlreadyProcessed(String eventId) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(PROCESSED_KEY, eventId)
        );
    }

    public boolean isEmailProcessed(String eventId) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(PROCESSED_EMAIL_KEY, eventId)
        );
    }

    public boolean isSmsProcessed(String eventId) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(PROCESSED_SMS_KEY, eventId)
        );
    }

    public boolean isPushProcessed(String eventId) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet().isMember(PROCESSED_PUSH_KEY, eventId)
        );
    }

    public void markProcessed(String eventId) {
        redisTemplate.opsForSet().add(PROCESSED_KEY, eventId);
    }

    public void markEmailProcessed(String eventId) {
        redisTemplate.opsForSet().add(PROCESSED_EMAIL_KEY, eventId);
    }

    public void markSmsProcessed(String eventId) {
        redisTemplate.opsForSet().add(PROCESSED_SMS_KEY, eventId);
    }

    public void markPushProcessed(String eventId) {
        redisTemplate.opsForSet().add(PROCESSED_PUSH_KEY, eventId);
    }
}
