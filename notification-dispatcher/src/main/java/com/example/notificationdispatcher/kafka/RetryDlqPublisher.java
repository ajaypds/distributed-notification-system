package com.example.notificationdispatcher.kafka;

import com.example.contract.NotificationEvent;
import com.example.notificationdispatcher.metrics.DispatcherMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RetryDlqPublisher {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private final DispatcherMetrics metrics;

    public void publishToRetry(NotificationEvent event, int retryCount) {

        metrics.incrementRetry();

        String retryTopic = switch (retryCount) {
            case 1 -> KafkaRetryConstants.RETRY_5S;
            case 2 -> KafkaRetryConstants.RETRY_15S;
            default -> KafkaRetryConstants.RETRY_30S;
        };

        log.info("Publishing event to retry topic: {}, retryTopic: {}", event.eventId(), retryTopic);
        kafkaTemplate.send(
                MessageBuilder
                        .withPayload(event)
                        .setHeader(KafkaHeaders.TOPIC, retryTopic)
                        .setHeader(KafkaHeaders.KEY, event.eventId())
                        .setHeader(KafkaRetryConstants.RETRY_COUNT_HEADER, retryCount)
                        .build()
        );
    }

    public void publishToDlq(NotificationEvent event, String reason) {
        log.info("Publishing event to DLQ: {}, reason: {}", event, reason);
        metrics.incrementDlq();
        kafkaTemplate.send(
                MessageBuilder
                        .withPayload(event)
                        .setHeader(KafkaHeaders.TOPIC, KafkaRetryConstants.DLQ_TOPIC)
                        .setHeader(KafkaHeaders.KEY, event.eventId())
                        .setHeader("dlq-reason", reason)
                        .build()
        );
    }
}
