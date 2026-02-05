package com.example.notificationdispatcher.service;

import com.example.contract.NotificationEvent;
import com.example.contract.UserPreferenceDTO;
import com.example.notificationdispatcher.client.*;
import com.example.notificationdispatcher.exception.PermanentFailureException;
import com.example.notificationdispatcher.exception.TransientFailureException;
import io.grpc.StatusRuntimeException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

import java.net.UnknownHostException;

@Service
@AllArgsConstructor
@Slf4j
public class DispatcherService {

    private final IdempotencyService idempotencyService;
    private final PreferenceClient preferenceClient;
    private final EmailGrpcClient emailClient;
    private final SmsGrpcClient smsClient;
    private final PushGrpcClient pushClient;

    public void process(NotificationEvent event) {

        if (idempotencyService.isAlreadyProcessed(event.eventId())) {
            log.info("Duplicate event detected, skipping processing for eventId: {}", event.eventId());
            return;
        }
        log.info("Consuming event: {}", event);

        try{

            UserPreferenceDTO pref = preferenceClient.fetch(event.userId());
            log.info("Start sending to clients");
            if (pref.emailEnabled()) {
                try{
                    if(idempotencyService.isEmailProcessed(event.eventId())) {
                        log.info("Email already sent for eventId: {}", event.eventId());
                    }else{
                        log.info("Sending event to EmailClient!");
                        emailClient.send(event.userId(), event.message());
                        log.info("Finished sending event to EmailClient!");
                        idempotencyService.markEmailProcessed(event.eventId());
                    }
                }
                catch(UnknownHostException ex){
                    log.error("Error occurred while sending event to EmailClient");
                    throw ex;
                }
            }
            if (pref.smsEnabled()) {

                try{
                    if(idempotencyService.isSmsProcessed(event.eventId())) {
                        log.info("SMS already sent for eventId: {}", event.eventId());
                    }else{
                        log.info("Sending event to SMSClient!");
                        smsClient.send(event.userId(), event.message());
                        log.info("Finished sending event to SMSClient!");
                        idempotencyService.markSmsProcessed(event.eventId());
                    }
                }
                catch(UnknownHostException ex){
                    log.error("Error occurred while sending event to SMSClient");
                    throw ex;
                }
            }
            if (pref.pushEnabled()) {

                try{
                    if(idempotencyService.isPushProcessed(event.eventId())) {
                        log.info("Push notification already sent for eventId: {}", event.eventId());
                    }else{
                        log.info("Sending event to PushClient!");
                        pushClient.send(event.userId(), event.message());
                        log.info("Finished sending event to PushClient!");
                        idempotencyService.markPushProcessed(event.eventId());
                    }
                }
                catch(UnknownHostException ex){
                    log.error("Error occurred while sending event to PushClient");
                    throw ex;
                }
            }
            idempotencyService.markProcessed(event.eventId());
        }
        catch (UnknownHostException | StatusRuntimeException ex) {
            // gRPC / network / downstream unavailable
            log.error("Transient failure occurred at DispatcherService!");
            throw new TransientFailureException("Downstream failure", ex);
        }
        catch(HttpServerErrorException ex){
            log.error("Permanent failure occurred at DispatcherService due to HttpServerErrorException!");
            throw new PermanentFailureException("HttpServerErrorException");
        }
        catch (Exception ex) {
            // bad data, mapping errors, etc.
            log.error("Permanent failure occurred at DispatcherService!",ex);
            throw new PermanentFailureException("Non-recoverable failure");
        }
    }
}

