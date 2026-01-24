package com.example.notificationdispatcher.client;

import com.example.notification.proto.NotificationRequest;
import com.example.notification.proto.NotificationServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.stereotype.Component;

import java.net.UnknownHostException;

@Component
public class EmailGrpcClient {

    private final NotificationServiceGrpc.NotificationServiceBlockingStub stub;

    public EmailGrpcClient() {
        ManagedChannel channel =
                ManagedChannelBuilder.forAddress("email-service", 9090)
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

