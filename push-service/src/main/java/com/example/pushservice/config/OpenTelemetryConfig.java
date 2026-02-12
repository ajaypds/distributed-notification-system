package com.example.pushservice.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.ServiceAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class OpenTelemetryConfig {

    @Bean
    public OpenTelemetry openTelemetry() {

        Resource resource = Resource.getDefault()
                .merge(Resource.create(
                        Attributes.of(
                                ServiceAttributes.SERVICE_NAME,
                                "push-service"
                        )
                ));

        SdkTracerProvider tracerProvider =
                SdkTracerProvider.builder()
                        .addSpanProcessor(
                                BatchSpanProcessor.builder(
                                        OtlpGrpcSpanExporter.builder()
                                                .setEndpoint("http://otel-collector:4317")
                                                .build()
                                ).build()
                        )
                        .setResource(resource)
                        .build();

        OpenTelemetrySdk sdk =
                OpenTelemetrySdk.builder()
                        .setTracerProvider(tracerProvider)
                        .setPropagators(
                                ContextPropagators.create(
                                        W3CTraceContextPropagator.getInstance()
                                )
                        )
                        .build();

        Runtime.getRuntime().addShutdownHook(
                new Thread(tracerProvider::close)
        );

        return sdk;
    }

    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer("push-service");
    }
}

