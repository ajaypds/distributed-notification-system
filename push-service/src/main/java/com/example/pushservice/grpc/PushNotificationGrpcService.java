package com.example.pushservice.grpc;

import com.example.notification.proto.NotificationRequest;
import com.example.notification.proto.NotificationResponse;
import com.example.notification.proto.NotificationServiceGrpc;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
public class PushNotificationGrpcService
        extends NotificationServiceGrpc.NotificationServiceImplBase{

    private final Random random = new Random();

    @Value("${email.failure-rate}")
    private double failureRate;

    @Value("${email.latency-ms}")
    private long latencyMs;

    @Override
    public void send(
            NotificationRequest request,
            StreamObserver<NotificationResponse> responseObserver){
        try{
            Thread.sleep(latencyMs);

            if(random.nextDouble() < failureRate){
                responseObserver.onError(
                        Status.UNAVAILABLE
                                .withDescription("Push service temporary unavailable")
                                .asRuntimeException()
                );
                return;
            }

            log.info("PUSH message sent to user " + request.getUserId() + ": " + request.getMessage());

            responseObserver.onNext(
                    NotificationResponse.newBuilder()
                            .setSuccess(true)
                            .build()
            );
            responseObserver.onCompleted();

        }catch(InterruptedException e){
            responseObserver.onError(
                    Status.INTERNAL
                            .withDescription("Interrupted")
                            .asRuntimeException()
            );
        }
    }
}