package com.example.notificationdispatcher.retry;

public final class RetryBackoffPolicy {

    private RetryBackoffPolicy() {}

    public static long backoffMillis(int retryCount) {
        // Exponential backoff
        // 1st retry: 5s
        // 2nd retry: 15s
        // 3rd retry: 30s
        return switch (retryCount) {
            case 1 -> 5_000;
            case 2 -> 15_000;
            default -> 30_000;
        };
    }
}
