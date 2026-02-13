package com.example.notificationdispatcher.consumer;

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
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class RetryEventConsumer {

    private final DispatcherService dispatcherService;
    private final RetryDlqPublisher retryDlqPublisher;
    private final OpenTelemetry openTelemetry;
    private final Tracer tracer;

    @KafkaListener(topics = KafkaRetryConstants.RETRY_5S)
    public void retry5s(ConsumerRecord<String, NotificationEvent> record) throws InterruptedException {
        NotificationEvent event = record.value();

        Context extractedContext = openTelemetry.getPropagators()
                        .getTextMapPropagator()
                                .extract(Context.current(), record.headers(),
                                        new KafkaHeadersGetter());
        Span span = tracer.spanBuilder("retry-5s consumer")
                        .setParent(extractedContext)
                                .setSpanKind(SpanKind.CONSUMER)
                                        .startSpan();
        span.setAttribute("notification.retry_count", 1);
        span.setAttribute("notification.retry_topic", "notification-events-retry-5s");
        log.info("Received event in retry5s topic: {}", event);
        Thread.sleep(5_000);

        try(Scope scope = span.makeCurrent()) {
            log.info("Calling dispatcher service in retry5s to process event!");
            dispatcherService.process(event, extractedContext);
            span.setStatus(StatusCode.OK, "Processed successfully");
            log.info("Event processed successfully in retry5s topic: {}", event);
        }
        catch (TransientFailureException ex) {
            span.recordException(ex);
            span.setStatus(StatusCode.ERROR, "Transient failure");
            log.info("Transient failure occurred at retry5s, publishing event to retry topic from RetryEventConsumer. retryCount: 2");
            retryDlqPublisher.publishToRetry(event, 2, extractedContext);
        }
        catch (PermanentFailureException ex) {
            span.recordException(ex);
            span.setStatus(StatusCode.ERROR, "Permanent failure");
            log.info("Permanent failure occurred at retry5s, publishing event to DLQ from RetryEventConsumer");
            retryDlqPublisher.publishToDlq(event, "PERMANENT_FAILURE");
        }finally{
            span.end();
        }
    }

    @KafkaListener(topics = KafkaRetryConstants.RETRY_15S)
    public void retry15s(ConsumerRecord<String, NotificationEvent> record) throws InterruptedException {
        NotificationEvent event = record.value();

        Context extractedContext = openTelemetry.getPropagators()
                .getTextMapPropagator()
                .extract(Context.current(), record.headers(),
                        new KafkaHeadersGetter());
        Span span = tracer.spanBuilder("retry-15s consumer")
                .setParent(extractedContext)
                .setSpanKind(SpanKind.CONSUMER)
                .startSpan();
        span.setAttribute("notification.retry_count", 2);
        span.setAttribute("notification.retry_topic", "notification-events-retry-15s");
        log.info("Received event in retry15s topic: {}", event);
        Thread.sleep(15_000);

        try(Scope scope = span.makeCurrent()) {
            log.info("Calling dispatcher service in retry15s to process event!");
            dispatcherService.process(event, extractedContext);
            span.setStatus(StatusCode.OK, "Processed successfully");
        }
        catch (TransientFailureException ex) {
            span.recordException(ex);
            span.setStatus(StatusCode.ERROR, "Transient failure");
            log.info("Transient failure occurred at retry15s, publishing event to retry topic from RetryEventConsumer. retryCount: 3");
            retryDlqPublisher.publishToRetry(event, 3, extractedContext);
        }
        catch (PermanentFailureException ex) {
            span.recordException(ex);
            span.setStatus(StatusCode.ERROR, "Permanent failure");
            log.info("Permanent failure occurred at retry15s, publishing event to DLQ from RetryEventConsumer");
            retryDlqPublisher.publishToDlq(event, "PERMANENT_FAILURE");
        }finally{
            span.end();
        }
    }

    @KafkaListener(topics = KafkaRetryConstants.RETRY_30S)
    public void retry30s(ConsumerRecord<String, NotificationEvent> record) throws InterruptedException {
        NotificationEvent event = record.value();

        Context extractedContext = openTelemetry.getPropagators()
                .getTextMapPropagator()
                .extract(Context.current(), record.headers(),
                        new KafkaHeadersGetter());
        Span span = tracer.spanBuilder("retry-30s consumer")
                .setParent(extractedContext)
                .setSpanKind(SpanKind.CONSUMER)
                .startSpan();
        span.setAttribute("notification.retry_count", 3);
        span.setAttribute("notification.retry_topic", "notification-events-retry-30s");
        log.info("Received event in retry30s topic: {}", event);
        Thread.sleep(30_000);

        try(Scope scope = span.makeCurrent()) {
            log.info("Calling dispatcher service in retry30s to process event!");
            dispatcherService.process(event, extractedContext);
            span.setStatus(StatusCode.OK, "Processed successfully");
        }
        catch (Exception ex) {
            span.recordException(ex);
            span.setStatus(StatusCode.ERROR, "Transient failure");
            log.info("Transient failure occurred at retry30s, max retries exhausted, publishing event to DLQ from RetryEventConsumer");
            retryDlqPublisher.publishToDlq(event, "RETRY_EXHAUSTED");
        }finally{
            span.end();
        }
    }

}
