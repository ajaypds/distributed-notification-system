package com.example.notificationdispatcher.service;

import com.example.contract.NotificationEvent;
import com.example.contract.UserPreferenceDTO;
import com.example.notificationdispatcher.client.*;
//import com.example.notificationdispatcher.event.NotificationEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class DispatcherService {

    private final IdempotencyService idempotencyService;
    private final PreferenceClient preferenceClient;
    private final EmailGrpcClient emailClient;
    private final SmsGrpcClient smsClient;
    private final PushGrpcClient pushClient;

//    public DispatcherService(
//            IdempotencyService idempotencyService,
//            PreferenceClient preferenceClient,
//            EmailGrpcClient emailClient,
//            SmsGrpcClient smsClient,
//            PushGrpcClient pushClient) {
//
//        this.idempotencyService = idempotencyService;
//        this.preferenceClient = preferenceClient;
//        this.emailClient = emailClient;
//        this.smsClient = smsClient;
//        this.pushClient = pushClient;
//    }

    public void process(NotificationEvent event) {

        if (idempotencyService.isDuplicate(event.eventId())) {
            return;
        }
        log.info("Dispatching event: {}", event);

        UserPreferenceDTO pref = preferenceClient.fetch(event.userId());

        if (pref.emailEnabled()) {
            emailClient.send(event.userId(), event.message());
        }
        if (pref.smsEnabled()) {
            smsClient.send(event.userId(), event.message());
        }
        if (pref.pushEnabled()) {
            pushClient.send(event.userId(), event.message());
        }
    }
}

