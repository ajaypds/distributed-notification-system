package com.example.preferences.tracing;

import io.opentelemetry.context.propagation.TextMapGetter;
import jakarta.servlet.http.HttpServletRequest;

import java.util.Collections;

public class HttpServletRequestGetter implements TextMapGetter<HttpServletRequest> {

    @Override
    public Iterable<String> keys(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames());
    }

    @Override
    public String get(HttpServletRequest request, String key) {

        return request.getHeader(key);
    }
}
