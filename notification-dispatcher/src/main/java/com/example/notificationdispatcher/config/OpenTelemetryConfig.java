package com.example.notificationdispatcher.config;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.ServiceAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


@Configuration
public class OpenTelemetryConfig {

    @Bean
    public OpenTelemetry openTelemetry() {

        // Define resource attributes for the service
        Resource resource = Resource.getDefault()
                .merge(Resource.create(
                        Attributes.of(
                                ServiceAttributes.SERVICE_NAME,
                                "notification-dispatcher"
                        )
                ));

        // Configure the OTLP exporter to send traces to the OpenTelemetry Collector
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

        // Build the OpenTelemetry SDK with the configured tracer provider and context propagators
        OpenTelemetrySdk sdk =
                OpenTelemetrySdk.builder()
                        .setTracerProvider(tracerProvider)
                        .setPropagators(
                                ContextPropagators.create(
                                        W3CTraceContextPropagator.getInstance()
                                )
                        )
                        .build();

        // Ensure that the tracer provider is properly shut down when the application exits
        Runtime.getRuntime().addShutdownHook(
                new Thread(tracerProvider::close)
        );

        // Return the configured OpenTelemetry instance for use in the application
        return sdk;
    }

    // Define a bean for RestTemplate that will be used to make HTTP calls to the user preferences service
    @Bean
    public RestTemplate restTemplate(OpenTelemetry openTelemetry) {

        RestTemplate template = new RestTemplate();

        // Add an interceptor to inject the current trace context into outgoing HTTP requests
        template.getInterceptors().add((request, body, execution) -> {

            // Inject the current trace context into the HTTP headers using the configured propagators
            openTelemetry.getPropagators()
                    .getTextMapPropagator()
                    .inject(
                            Context.current(),
                            request.getHeaders(),
                            (headers, key, value) -> headers.add(key, value)
                    );

            // Proceed with the HTTP request execution
            return execution.execute(request, body);
        });

        // Return the configured RestTemplate for use in the application
        return template;
    }


    // Define a bean for the OpenTelemetry Tracer, which will be used to create spans in the application
    @Bean
    public Tracer tracer(OpenTelemetry openTelemetry) {
        return openTelemetry.getTracer("notification-dispatcher");
    }
}

