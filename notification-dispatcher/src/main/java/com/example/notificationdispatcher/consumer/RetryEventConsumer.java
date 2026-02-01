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

    @KafkaListener(topics = KafkaRetryConstants.RETRY_TOPIC)
    public void retry(
            NotificationEvent event,
            @Header(KafkaRetryConstants.RETRY_COUNT_HEADER) int retryCount,
            @Header(KafkaRetryConstants.NEXT_RETRY_AT_HEADER) long nextRetryAt) {

        long now = System.currentTimeMillis();

        if(now < nextRetryAt){
            retryDlqPublisher.publishToRetry(event, retryCount);
            return;
        }

        try {
            log.info("Calling dispatcher service in RetryEventConsumer to process event!");
            dispatcherService.process(event);
        }
        catch (TransientFailureException ex) {
            log.error("Exception failure occurred at RetryEventConsumer");
            if (retryCount < KafkaRetryConstants.MAX_RETRIES) {
                retryDlqPublisher.publishToRetry(event, retryCount + 1);
            } else {
                retryDlqPublisher.publishToDlq(event, "RETRY_EXHAUSTED");
            }
        }
        catch(PermanentFailureException ex){
            retryDlqPublisher.publishToDlq(event, "PERMANENT FAILURE");
        }
    }
}
