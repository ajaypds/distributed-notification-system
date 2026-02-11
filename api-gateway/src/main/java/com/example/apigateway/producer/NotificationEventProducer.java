package com.example.apigateway.producer;

import com.example.apigateway.dto.NotificationRequest;
//import com.example.apigateway.event.NotificationEvent;
import com.example.contract.NotificationEvent;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventProducer {

    private static final String TOPIC = "notification-events";
    private final Tracer tracer;
    private final OpenTelemetry openTelemetry;

    private final KafkaTemplate<String, NotificationEvent> kafkaTemplate;

//    public void publish2(NotificationRequest request){
//        try{
//            kafkaTemplate.executeInTransaction(kt -> {
//                NotificationEvent event = NotificationEvent.v1(request.userId(), request.message());
//                log.info("Publishing notification event: {}", event);
//                kt.send(TOPIC, event.eventId(), event);
//                return true;
//            });
//        }catch (Exception e){
//            throw new RuntimeException("Failed to publish notification event", e);
//        }
//    }


    public void publish(NotificationRequest request) {
        Span span = tracer.spanBuilder("kafka.produce.notification")
                .setSpanKind(SpanKind.PRODUCER)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            kafkaTemplate.executeInTransaction(kt -> {
                NotificationEvent event = NotificationEvent.v1(
                        request.userId(), request.message()
                );

                ProducerRecord<String, NotificationEvent> record =
                        new ProducerRecord<>(TOPIC, event.eventId(), event);

                // Inject trace context into headers
                openTelemetry.getPropagators()
                        .getTextMapPropagator()
                        .inject(Context.current(), record.headers(),
                                (headers, key, value) -> headers.add(key, value.getBytes()));

                log.info("Publishing notification event: {}", event);
                kt.send(record);

                return true;
            });
        } catch (Exception e) {
            span.recordException(e);
            throw new RuntimeException("Failed to publish notification event", e);
        } finally {
            span.end();
        }
    }


}
