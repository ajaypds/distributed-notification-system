package com.example.notificationdispatcher.kafka;

public final class KafkaRetryConstants {

    private KafkaRetryConstants() {}

    public static final String RETRY_COUNT_HEADER = "retry-count";

    public static final int MAX_RETRIES = 3;

    public static final String MAIN_TOPIC = "notification-events";
    public static final String RETRY_TOPIC = "notification-events-retry";
    public static final String DLQ_TOPIC = "notification-events-dlq";
}
