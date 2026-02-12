package com.example.notificationdispatcher.kafka;

import io.opentelemetry.context.propagation.TextMapGetter;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class KafkaHeadersGetter implements TextMapGetter<Headers> {
    @Override
    public Iterable<String> keys(Headers carrier) {
        return StreamSupport.stream(carrier.spliterator(), false)
                .map(Header::key)
                .collect(Collectors.toList());
    }

    @Override
    public String get(Headers carrier, String key) {
        Header h = carrier.lastHeader(key);
        return h == null ? null : new String(h.value());
    }
}
