package com.example.notificationdispatcher.client;

import com.example.notification.proto.NotificationRequest;
import com.example.notification.proto.NotificationServiceGrpc;
import com.example.notificationdispatcher.grpc.GrpcClientTracingInterceptor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.opentelemetry.api.OpenTelemetry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class PushGrpcClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(2);
    private static final int MAX_ATTEMPTS = 2;

    private final NotificationServiceGrpc.NotificationServiceBlockingStub stub;

    public PushGrpcClient(OpenTelemetry openTelemetry) {
        log.info("OpenTelemetry configured for PushGrpcClient: " + openTelemetry);
        ManagedChannel channel =
                ManagedChannelBuilder.forAddress("push-service", 9090)
                        .usePlaintext()
                        .intercept(new GrpcClientTracingInterceptor(openTelemetry))
                        .build();

        this.stub = NotificationServiceGrpc.newBlockingStub(channel);
    }

//    public void send(String userId, String message) throws UnknownHostException {
//        stub.send(
//                NotificationRequest.newBuilder()
//                        .setUserId(userId)
//                        .setMessage(message)
//                        .build()
//        );
//    }

    public void send(String userId, String message){

        NotificationRequest request = NotificationRequest.newBuilder()
                .setUserId(userId)
                .setMessage(message)
                .build();

        int attempt = 0;

        while(true){
            attempt++;
            try{
                log.info("Attempt " + attempt + " to send push message to user " + userId);
                var response = stub
                        .withDeadlineAfter(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
                        .send(request);
                log.info("Push message sent to user " + userId);
                return; // Success

            }catch(StatusRuntimeException ex){
                if(isRetryable(ex) && attempt < MAX_ATTEMPTS){
                    continue;
                }

                throw ex;
            }
        }
    }

    private boolean isRetryable(StatusRuntimeException ex){
        return ex.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED || ex.getStatus().getCode() == Status.Code.UNAVAILABLE;
    }
}

