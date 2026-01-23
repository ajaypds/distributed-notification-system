package com.example.contract;

import java.time.Instant;
import java.util.UUID;

public record NotificationEvent(
        String eventId,
        int schemaVersion,
        String userId,
        String message,
        Instant createdAt
) {
    public static NotificationEvent v1(String userId, String message){
        return new NotificationEvent(
                UUID.randomUUID().toString(),
                1,
                userId,
                message,
                Instant.now()
        );
    }
}
