package com.example.apigateway.producer;

import com.example.apigateway.dto.NotificationRequest;
import com.example.apigateway.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventProducer {

    private static final String TOPIC = "notification-events";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publish(NotificationRequest request){
        try{
            NotificationEvent event = NotificationEvent.v1(request.userId(), request.message());
            log.info("Publishing notification event: {}", event);
            kafkaTemplate.send(TOPIC, event.eventId(), event);
        }catch (Exception e){
            throw new RuntimeException("Failed to publish notification event", e);
        }

    }
}
