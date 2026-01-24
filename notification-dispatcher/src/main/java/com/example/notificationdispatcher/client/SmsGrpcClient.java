package com.example.notificationdispatcher.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import com.example.notification.proto.NotificationRequest;
import com.example.notification.proto.NotificationServiceGrpc;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;

@Component
public class SmsGrpcClient {

    private final NotificationServiceGrpc.NotificationServiceBlockingStub stub;

    public SmsGrpcClient() {
        ManagedChannel channel =
                ManagedChannelBuilder.forAddress("sms-service", 9090)
                        .usePlaintext()
                        .build();

        this.stub = NotificationServiceGrpc.newBlockingStub(channel);
    }

    public void send(String userId, String message) throws UnknownHostException {
        stub.send(
                NotificationRequest.newBuilder()
                        .setUserId(userId)
                        .setMessage(message)
                        .build()
        );
    }
}

