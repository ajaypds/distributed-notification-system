package com.example.notificationdispatcher.event;

import java.time.Instant;

public record NotificationEvent(
        String eventId,
        int schemaVersion,
        String userId,
        String message,
        Instant createdAt
) {}
