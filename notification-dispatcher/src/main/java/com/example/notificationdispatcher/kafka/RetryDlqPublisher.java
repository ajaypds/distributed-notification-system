package com.example.notificationdispatcher.kafka;

import com.example.contract.NotificationEvent;
import com.example.notificationdispatcher.metrics.DispatcherMetrics;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
public class RetryDlqPublisher {

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;
    private final DispatcherMetrics metrics;
    private final OpenTelemetry openTelemetry;
    private final Tracer tracer;

    public void publishToRetry(NotificationEvent event, int retryCount, Context parentContext) {

        metrics.incrementRetry();

        String retryTopic = switch (retryCount) {
            case 1 -> KafkaRetryConstants.RETRY_5S;
            case 2 -> KafkaRetryConstants.RETRY_15S;
            default -> KafkaRetryConstants.RETRY_30S;
        };
        log.info("Publishing to : " + retryTopic);

        Span span = tracer.spanBuilder("Publishing to : " + retryTopic)
                        .setParent(parentContext)
                        .setSpanKind(SpanKind.PRODUCER)
                                .startSpan();
        try(Scope scope = span.makeCurrent()){
            ProducerRecord<String, NotificationEvent> record = new ProducerRecord<>(retryTopic, event.eventId(), event);
            record.headers().add(KafkaRetryConstants.RETRY_COUNT_HEADER, String.valueOf(retryCount).getBytes(StandardCharsets.UTF_8));

            openTelemetry.getPropagators()
                    .getTextMapPropagator()
                    .inject(Context.current(), record.headers(),
                            (headers, key, value)->headers.add(key, value.getBytes()));
            kafkaTemplate.send(record);
            span.setAttribute("notification.event_id", event.eventId());
            span.setAttribute("notification.user_id", event.userId());
            span.setAttribute("notification.schema_version", event.schemaVersion());
            span.setAttribute("TraceId", span.getSpanContext().getTraceId());
            span.setAttribute("Retry Topic", retryTopic);
            span.setStatus(StatusCode.OK, "Published to retry topic successfully");
            log.info("Published event to retry topic successfully: {}, retryTopic: {}", event.eventId());
        }catch(Exception ex){
            span.recordException(ex);
            span.setStatus(StatusCode.ERROR, "Failed to publish retry event");
            log.info("Failed to publish event to retry topic: {}, retryTopic: {}, error: {}", event.eventId(), retryTopic, ex.getMessage());
        }finally{
            span.end();
        }
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
