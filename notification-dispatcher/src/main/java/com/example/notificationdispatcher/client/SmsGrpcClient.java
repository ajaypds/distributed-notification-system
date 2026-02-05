package com.example.notificationdispatcher.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import com.example.notification.proto.NotificationRequest;
import com.example.notification.proto.NotificationServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class SmsGrpcClient {

    private static final Duration TIMEOUT = Duration.ofSeconds(2);
    private static final int MAX_ATTEMPTS = 2;

    private final NotificationServiceGrpc.NotificationServiceBlockingStub stub;

    public SmsGrpcClient() {
        ManagedChannel channel =
                ManagedChannelBuilder.forAddress("sms-service", 9090)
                        .usePlaintext()
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
                log.info("Attempt " + attempt + " to send SMS message to user " + userId);
                var response = stub
                        .withDeadlineAfter(TIMEOUT.toMillis(), TimeUnit.MILLISECONDS)
                        .send(request);
                log.info("SMS message sent to user " + userId);
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

