package com.example.notificationdispatcher.metrics;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class DispatcherMetrics {
    
    private final Counter dlqReplayCounter;

    private final MeterRegistry registry;
    private final Counter dlqCounter;
    private final Counter successCounter;
    private final Counter failureCounter;

    public DispatcherMetrics(MeterRegistry registry){
        this.registry = registry;

        this.dlqReplayCounter = Counter.builder("dispatcher_dlq_replay_total")
                .description("Total DLQ Replay events")
                .register(registry);

        this.successCounter = Counter.builder("dispatcher_processed_success_total")
                .description("Total successfully processed events")
                .register(registry);

        this.failureCounter = Counter.builder("dispatcher_processed_failure_total")
                .description("Total permanent failures")
                .register(registry);

        this.dlqCounter = Counter.builder("dispatcher_dlq_total")
                .description("Total DLQ events")
                .register(registry);
    }

    public void incrementRetry(String retryLevel){
        Counter.builder("dispatcher_retry_total")
                .description("Total retry attempts")
                .tag("retry_level", retryLevel)
                .register(registry)
                .increment();
    }

    public void incrementDlq(){
        dlqCounter.increment();
    }

    public void incrementDlqReplay(){
        dlqReplayCounter.increment();
    }

    public void incrementSuccess() {
        successCounter.increment();
    }

    public void incrementFailure() {
        failureCounter.increment();
    }
}
