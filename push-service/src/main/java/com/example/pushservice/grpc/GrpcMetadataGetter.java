package com.example.pushservice.grpc;

import io.grpc.Metadata;
import io.opentelemetry.context.propagation.TextMapGetter;

public class GrpcMetadataGetter implements TextMapGetter<Metadata> {

    @Override
    public Iterable<String> keys(Metadata carrier) {
        return carrier.keys();
    }

    @Override
    public String get(Metadata carrier, String key) {
        if (carrier == null || key == null) {
            return null;
        }
        Metadata.Key<String> metadataKey =
                Metadata.Key.of(key, Metadata.ASCII_STRING_MARSHALLER);
        return carrier.get(metadataKey);
    }
}
