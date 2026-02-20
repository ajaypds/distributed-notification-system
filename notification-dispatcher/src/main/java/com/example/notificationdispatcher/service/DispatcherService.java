package com.example.notificationdispatcher.service;

import com.example.contract.NotificationEvent;
import com.example.contract.UserPreferenceDTO;
import com.example.notificationdispatcher.client.*;
import com.example.notificationdispatcher.exception.PermanentFailureException;
import com.example.notificationdispatcher.exception.TransientFailureException;
import com.example.notificationdispatcher.metrics.DispatcherMetrics;
import io.grpc.StatusRuntimeException;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;

@Service
@AllArgsConstructor
@Slf4j
public class DispatcherService {

    private final IdempotencyService idempotencyService;
    private final PreferenceClient preferenceClient;
    private final EmailGrpcClient emailClient;
    private final SmsGrpcClient smsClient;
    private final PushGrpcClient pushClient;
    private final DispatcherMetrics metrics;
    private final Tracer tracer;

    public void process(NotificationEvent event, Context context) {

        if (idempotencyService.isAlreadyProcessed(event.eventId())) {
            log.info("Duplicate event detected, skipping processing for eventId: {}", event.eventId());
            return;
        }
        Span span = tracer.spanBuilder("dispatcher.process")
                .setParent(context)
                .setSpanKind(SpanKind.INTERNAL)
                .startSpan();
        span.setAttribute("notification.event_id", event.eventId());
        log.info("Processing event: {} in DispatcherService", event);

        try{

            UserPreferenceDTO pref = preferenceClient.fetch(event.userId());
            log.info("Start sending to clients");
            if (pref.emailEnabled()) {
                try{
                    if(idempotencyService.isEmailProcessed(event.eventId())) {
                        log.info("Email already sent for eventId: {}", event.eventId());
                        span.setAttribute("email.sent", false);
                        span.setAttribute("email.skipped", true);
                        span.addEvent("email_skipped_due_to_idempotency");
                    }else{
                        emailClient.send(event.userId(), event.message());
                        idempotencyService.markEmailProcessed(event.eventId());
                        span.setAttribute("email.sent", true);
                        span.addEvent("email_sent_successfully");
                    }
                }
                catch(StatusRuntimeException ex){
                    span.setAttribute("email.sent", false);
                    span.addEvent("email_send_failure");
                    log.error("Error occurred while sending event to EmailClient");
                    throw ex;
                }
            }
            if (pref.smsEnabled()) {

                try{
                    if(idempotencyService.isSmsProcessed(event.eventId())) {
                        log.info("SMS already sent for eventId: {}", event.eventId());
                        span.setAttribute("sms.sent", false);
                        span.setAttribute("sms.skipped", true);
                        span.addEvent("sms_skipped_due_to_idempotency");
                    }else{
                        smsClient.send(event.userId(), event.message());
                        idempotencyService.markSmsProcessed(event.eventId());
                        span.setAttribute("sms.sent", true);
                        span.addEvent("sms_sent_successfully");
                    }
                }
                catch(StatusRuntimeException ex){
                    span.setAttribute("sms.sent", false);
                    log.error("Error occurred while sending event to SMSClient");
                    span.addEvent("sms_send_failure");
                    throw ex;
                }
            }
            if (pref.pushEnabled()) {

                try{
                    if(idempotencyService.isPushProcessed(event.eventId())) {
                        log.info("Push notification already sent for eventId: {}", event.eventId());
                        span.setAttribute("push.sent", false);
                        span.setAttribute("push.skipped", true);
                        span.addEvent("push_skipped_due_to_idempotency");
                    }else{
                        pushClient.send(event.userId(), event.message());
                        idempotencyService.markPushProcessed(event.eventId());
                        span.setAttribute("push.sent", true);
                        span.addEvent("push_sent_successfully");
                    }
                }
                catch(StatusRuntimeException ex){
                    log.error("Error occurred while sending event to PushClient");
                    span.setAttribute("push.sent", false);
                    span.addEvent("push_send_failure");
                    throw ex;
                }
            }
            idempotencyService.markProcessed(event.eventId());
            metrics.incrementSuccess();
            span.setStatus(StatusCode.OK);
            span.addEvent("event_processed_successfully");
        }
        catch (StatusRuntimeException ex) {
            // gRPC / network / downstream unavailable
            log.error("Transient failure occurred at DispatcherService!");
            span.setAttribute("failure.type", "transient");
            span.setStatus(StatusCode.ERROR);
            span.addEvent("transient_failure");
            throw new TransientFailureException("Downstream failure", ex);
        }
        catch(HttpServerErrorException ex){
            log.error("Permanent failure occurred at DispatcherService due to HttpServerErrorException!");
            span.setAttribute("failure.type", "http_server_error");
            span.setStatus(StatusCode.ERROR);
            span.addEvent("permanent_failure_http_server_error");
            throw new PermanentFailureException("HttpServerErrorException");
        }
        catch (Exception ex) {
            // bad data, mapping errors, etc.
            metrics.incrementFailure();
            log.error("Permanent failure occurred at DispatcherService!",ex);
            span.setAttribute("failure.type", "permanent");
            span.setStatus(StatusCode.ERROR);
            span.addEvent("permanent_failure");
            throw new PermanentFailureException("Non-recoverable failure");
        }
        finally {
            span.end();
        }
    }
}

