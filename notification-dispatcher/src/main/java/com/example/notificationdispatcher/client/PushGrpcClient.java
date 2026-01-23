package com.example.notificationdispatcher.client;

import com.example.notification.proto.NotificationRequest;
import com.example.notification.proto.NotificationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class PushGrpcClient {

    private final NotificationServiceGrpc.NotificationServiceBlockingStub stub;

    public PushGrpcClient() {
        ManagedChannel channel =
                ManagedChannelBuilder.forAddress("push-service", 9090)
                        .usePlaintext()
                        .build();

        this.stub = NotificationServiceGrpc.newBlockingStub(channel);
    }

    public void send(String userId, String message) {
        stub.send(
                NotificationRequest.newBuilder()
                        .setUserId(userId)
                        .setMessage(message)
                        .build()
        );
    }
}

