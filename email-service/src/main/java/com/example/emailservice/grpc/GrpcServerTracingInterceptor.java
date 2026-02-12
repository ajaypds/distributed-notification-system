package com.example.emailservice.grpc;

import io.grpc.*;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

@GrpcGlobalServerInterceptor
@RequiredArgsConstructor
@Slf4j
public class GrpcServerTracingInterceptor implements ServerInterceptor {

    private final OpenTelemetry openTelemetry;
    private final Tracer tracer;

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT>
    interceptCall(ServerCall<ReqT, RespT> call,
                  Metadata headers,
                  ServerCallHandler<ReqT, RespT> next) {

        Context extracted =
                openTelemetry.getPropagators()
                        .getTextMapPropagator()
                        .extract(
                                Context.current(),
                                headers,
                                new GrpcMetadataGetter()
                        );
        log.info("Extracted trace context: " + extracted);
        Span span = tracer.spanBuilder(call.getMethodDescriptor().getFullMethodName())
                .setParent(extracted)
                .setSpanKind(SpanKind.SERVER)
                .startSpan();
        Scope scope = span.makeCurrent();

        return new ForwardingServerCallListener
                .SimpleForwardingServerCallListener<>(next.startCall(call, headers)) {

            @Override
            public void onComplete() {
                span.end();
                scope.close();
                super.onComplete();
            }

            @Override
            public void onCancel() {
                span.end();
                scope.close();
                super.onCancel();
            }
        };
    }
}

