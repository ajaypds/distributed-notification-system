package com.example.notificationdispatcher.kafka;

import com.example.contract.NotificationEvent;
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

    public void publishToRetry(NotificationEvent event, int retryCount) {
        log.info("Publishing event to retry topic: {}, retryCount: {}", event.eventId(), retryCount);
        kafkaTemplate.send(
                MessageBuilder
                        .withPayload(event)
                        .setHeader(KafkaHeaders.TOPIC, KafkaRetryConstants.RETRY_TOPIC)
                        .setHeader(KafkaHeaders.KEY, event.eventId())
                        .setHeader(KafkaRetryConstants.RETRY_COUNT_HEADER, retryCount)
                        .build()
        );
    }

    public void publishToDlq(NotificationEvent event, String reason) {
        log.info("Publishing event to DLQ: {}, reason: {}", event, reason);
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
