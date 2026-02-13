package com.example.notificationdispatcher.consumer;

//import com.example.notificationdispatcher.event.NotificationEvent;
import com.example.contract.NotificationEvent;
import com.example.notificationdispatcher.exception.PermanentFailureException;
import com.example.notificationdispatcher.exception.TransientFailureException;
import com.example.notificationdispatcher.kafka.KafkaHeadersGetter;
import com.example.notificationdispatcher.kafka.KafkaRetryConstants;
import com.example.notificationdispatcher.kafka.RetryDlqPublisher;
import com.example.notificationdispatcher.service.DispatcherService;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@AllArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final DispatcherService dispatcherService;
    private final RetryDlqPublisher retryDlqPublisher;
    private final OpenTelemetry openTelemetry;
    private final Tracer tracer;

//    @KafkaListener(topics = KafkaRetryConstants.MAIN_TOPIC)
//    public void consume(
//            NotificationEvent event,
//            @Header(
//                    value = KafkaRetryConstants.RETRY_COUNT_HEADER,
//                    required = false
//            ) Integer retryCount) {
//
//        int currentRetry = retryCount == null ? 0 : retryCount;
//        try{
//            log.info("Calling dispatcher service in NotificationEventConsumer to process event!");
//            dispatcherService.process(event);
//        } catch(TransientFailureException ex){
//            log.error("Transient failure occurred at NotificationEventConsumer");
//            if(currentRetry < KafkaRetryConstants.MAX_RETRIES){
//                log.info("Publishing event to retry topic from NotificationEventConsumer, currentRetry: {}", currentRetry);
//                retryDlqPublisher.publishToRetry(event, currentRetry + 1);
//            }else{
//                log.info("Max retries exceeded, publishing event to DLQ from NotificationEventConsumer");
//                retryDlqPublisher.publishToDlq(event, "MAX_RETRIES_EXCEEDED");
//            }
//        }catch(PermanentFailureException ex){
//            log.error("Permanent failure occurred at NotificationEventConsumer");
//            retryDlqPublisher.publishToDlq(event, "PERMANENT FAILURE");
//        }
//    }

//    @KafkaListener(topics = KafkaRetryConstants.MAIN_TOPIC)
//    public void consume(
//            NotificationEvent event,
//            Headers headers) {
//
//        Context extractedContext =
//                openTelemetry.getPropagators()
//                        .getTextMapPropagator()
//                        .extract(
//                                Context.current(),
//                                headers,
//                                new KafkaHeadersGetter()
//                        );
//
//        Span span = tracer.spanBuilder("kafka.consume.notification")
//                .setParent(extractedContext)
//                .setSpanKind(SpanKind.CONSUMER)
//                .startSpan();
//
//        try (Scope scope = span.makeCurrent()) {
//            dispatcherService.process(event);
//        } catch (Exception ex) {
//            span.recordException(ex);
//            throw ex;
//        } finally {
//            span.end();
//        }
//    }

    @KafkaListener(topics = KafkaRetryConstants.MAIN_TOPIC)
    public void consume(
            ConsumerRecord<String, NotificationEvent> record) {

        NotificationEvent event = record.value();
        Headers headers = record.headers();
        Header retryCount = record.headers().lastHeader(KafkaRetryConstants.RETRY_COUNT_HEADER);
        int currentRetry = 0;
        if (retryCount != null) {
            currentRetry = Integer.parseInt(new String(retryCount.value()));
        }


        Context extractedContext =
                openTelemetry.getPropagators()
                        .getTextMapPropagator()
                        .extract(
                                Context.current(),
                                headers,
                                new KafkaHeadersGetter()
                        );

        Span span = tracer.spanBuilder("kafka.consume.notification")
                .setParent(extractedContext)
                .setSpanKind(SpanKind.CONSUMER)
                .startSpan();
        span.setAttribute("notification.event_id", event.eventId());
        span.setAttribute("notification.user_id", event.userId());
        span.setAttribute("notification.schema_version", event.schemaVersion());

        try (Scope scope = span.makeCurrent()) {
            dispatcherService.process(event, extractedContext);
            span.setStatus(StatusCode.OK, "Event processed successfully");
        }catch(TransientFailureException ex){
            log.error("Transient failure occurred at NotificationEventConsumer");
            if(currentRetry < KafkaRetryConstants.MAX_RETRIES){
                log.info("Publishing event to retry topic from NotificationEventConsumer, currentRetry: {}", currentRetry);
                retryDlqPublisher.publishToRetry(event, currentRetry + 1, extractedContext);
                span.addEvent("retry_scheduled");
            }else{
                log.info("Max retries exceeded, publishing event to DLQ from NotificationEventConsumer");
                retryDlqPublisher.publishToDlq(event, "MAX_RETRIES_EXCEEDED");
                span.addEvent("dlq_published");
            }
            span.recordException(ex);
        }catch(PermanentFailureException ex){
            log.error("Permanent failure occurred at NotificationEventConsumer");
            retryDlqPublisher.publishToDlq(event, "PERMANENT FAILURE");
            span.recordException(ex);
        } catch (Exception ex) {
            span.recordException(ex);
            throw ex;
        } finally {
            span.end();
        }
    }

}
