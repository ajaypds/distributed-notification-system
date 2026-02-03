package com.example.notificationdispatcher.consumer;

import com.example.contract.NotificationEvent;
import com.example.notificationdispatcher.exception.PermanentFailureException;
import com.example.notificationdispatcher.exception.TransientFailureException;
import com.example.notificationdispatcher.kafka.KafkaRetryConstants;
import com.example.notificationdispatcher.kafka.RetryDlqPublisher;
import com.example.notificationdispatcher.service.DispatcherService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class RetryEventConsumer {

    private final DispatcherService dispatcherService;
    private final RetryDlqPublisher retryDlqPublisher;

    @KafkaListener(topics = KafkaRetryConstants.RETRY_5S)
    public void retry5s(NotificationEvent event) throws InterruptedException {
        log.info("Received event in retry5s topic: {}", event);
        Thread.sleep(5_000);

        try {
            log.info("Calling dispatcher service in retry5s to process event!");
            dispatcherService.process(event);
            log.info("Event processed successfully in retry5s topic: {}", event);
        }
        catch (TransientFailureException ex) {
            log.info("Transient failure occurred at retry5s, publishing event to retry topic from RetryEventConsumer. retryCount: 2");
            retryDlqPublisher.publishToRetry(event, 2);
        }
        catch (PermanentFailureException ex) {
            log.info("Permanent failure occurred at retry5s, publishing event to DLQ from RetryEventConsumer");
            retryDlqPublisher.publishToDlq(event, "PERMANENT_FAILURE");
        }
    }

    @KafkaListener(topics = KafkaRetryConstants.RETRY_15S)
    public void retry15s(NotificationEvent event) throws InterruptedException {
        log.info("Received event in retry15s topic: {}", event);
        Thread.sleep(15_000);

        try {
            log.info("Calling dispatcher service in retry15s to process event!");
            dispatcherService.process(event);
        }
        catch (TransientFailureException ex) {
            log.info("Transient failure occurred at retry15s, publishing event to retry topic from RetryEventConsumer. retryCount: 3");
            retryDlqPublisher.publishToRetry(event, 3);
        }
        catch (PermanentFailureException ex) {
            log.info("Permanent failure occurred at retry15s, publishing event to DLQ from RetryEventConsumer");
            retryDlqPublisher.publishToDlq(event, "PERMANENT_FAILURE");
        }
    }

    @KafkaListener(topics = KafkaRetryConstants.RETRY_30S)
    public void retry30s(NotificationEvent event) throws InterruptedException {
        log.info("Received event in retry30s topic: {}", event);
        Thread.sleep(30_000);

        try {
            log.info("Calling dispatcher service in retry30s to process event!");
            dispatcherService.process(event);
        }
        catch (Exception ex) {
            log.info("Transient failure occurred at retry30s, max retries exhausted, publishing event to DLQ from RetryEventConsumer");
            retryDlqPublisher.publishToDlq(event, "RETRY_EXHAUSTED");
        }
    }

}
