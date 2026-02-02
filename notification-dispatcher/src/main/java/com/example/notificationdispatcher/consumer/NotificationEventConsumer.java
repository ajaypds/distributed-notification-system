package com.example.notificationdispatcher.consumer;

//import com.example.notificationdispatcher.event.NotificationEvent;
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
public class NotificationEventConsumer {

    private final DispatcherService dispatcherService;
    private final RetryDlqPublisher retryDlqPublisher;

    @KafkaListener(topics = KafkaRetryConstants.MAIN_TOPIC)
    public void consume(
            NotificationEvent event,
            @Header(
                    value = KafkaRetryConstants.RETRY_COUNT_HEADER,
                    required = false
            ) Integer retryCount) {

        int currentRetry = retryCount == null ? 0 : retryCount;
        try{
            log.info("Calling dispatcher service in NotificationEventConsumer to process event!");
            dispatcherService.process(event);
        } catch(TransientFailureException ex){
            log.error("Transient failure occurred at NotificationEventConsumer");
            if(currentRetry < KafkaRetryConstants.MAX_RETRIES){
                log.info("Publishing event to retry topic from NotificationEventConsumer, currentRetry: {}", currentRetry);
                retryDlqPublisher.publishToRetry(event, currentRetry + 1);
            }else{
                log.info("Max retries exceeded, publishing event to DLQ from NotificationEventConsumer");
                retryDlqPublisher.publishToDlq(event, "MAX_RETRIES_EXCEEDED");
            }
        }catch(PermanentFailureException ex){
            log.error("Permanent failure occurred at NotificationEventConsumer");
            retryDlqPublisher.publishToDlq(event, "PERMANENT FAILURE");
        }
    }
}
