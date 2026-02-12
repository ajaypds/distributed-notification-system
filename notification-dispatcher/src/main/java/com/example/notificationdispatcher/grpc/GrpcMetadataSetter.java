package com.example.notificationdispatcher.grpc;

import io.opentelemetry.context.propagation.TextMapSetter;
import io.grpc.Metadata;
import org.jspecify.annotations.NonNull;

public class GrpcMetadataSetter implements TextMapSetter<Metadata> {

    @Override
    public void set(Metadata carrier, @NonNull String key, @NonNull String value) {
        if (carrier == null) {
            return;
        }
        Metadata.Key<String> metadataKey =
                Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER);
        carrier.put(metadataKey, value);
    }
}
