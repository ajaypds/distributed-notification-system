package com.example.preferences.tracing;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TracingFilter extends OncePerRequestFilter {

    private final OpenTelemetry openTelemetry;
    private final Tracer tracer;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        Context extracted =
                openTelemetry.getPropagators()
                        .getTextMapPropagator()
                        .extract(
                                Context.current(),
                                request,
                                new HttpServletRequestGetter()
                        );

        Span span = tracer.spanBuilder(request.getMethod() + " " + request.getRequestURI())
                .setParent(extracted)
                .setSpanKind(SpanKind.SERVER)
                .startSpan();

        try (Scope scope = span.makeCurrent()) {
            filterChain.doFilter(request, response);
        } finally {
            span.end();
        }
    }
}
