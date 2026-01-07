package com.example.apigateway.controller;

import com.example.apigateway.dto.NotificationRequest;
import com.example.apigateway.producer.NotificationEventProducer;
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

    @PostMapping
    public ResponseEntity<Void> publish(@Valid @RequestBody NotificationRequest request){
        log.info("Received notification request for userId: {}", request.userId());
        producer.publish(request);
        log.info("Published notification event for userId: {}", request.userId());
        return ResponseEntity.accepted().build();
    }
}
