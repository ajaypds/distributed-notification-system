package com.example.apigateway.controller;

import com.example.apigateway.dto.NotificationRequest;
import com.example.apigateway.producer.NotificationEventProducer;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationEventProducer producer;
    private final Tracer tracer;

//    @PostMapping
//    public ResponseEntity<Void> publish(@Valid @RequestBody NotificationRequest request){
//        log.info("Received notification request for userId: {}", request.userId());
//        producer.publish(request);
//        log.info("Published notification event for userId: {}", request.userId());
//        return ResponseEntity.accepted().build();
//    }

    @PostMapping
    public ResponseEntity<Void> publish(
            @RequestBody NotificationRequest request) {

        Span span = tracer.spanBuilder("POST /api/notifications")
                .setSpanKind(SpanKind.SERVER)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            log.info("Received notification request for userId: {}", request.userId());
            producer.publish(request);
            log.info("Published notification event for userId: {}", request.userId());
            return ResponseEntity.accepted().build();

        } catch (Exception ex) {
            span.recordException(ex);
            throw ex;
        } finally {
            span.end();
        }
    }


}
