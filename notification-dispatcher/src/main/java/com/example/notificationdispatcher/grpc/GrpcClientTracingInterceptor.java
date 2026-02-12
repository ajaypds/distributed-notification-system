package com.example.notificationdispatcher.grpc;

import io.grpc.*;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.context.Context;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GrpcClientTracingInterceptor implements ClientInterceptor {

    private final OpenTelemetry openTelemetry;

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT>
    interceptCall(MethodDescriptor<ReqT, RespT> method,
                  CallOptions callOptions,
                  Channel next) {

        return new ForwardingClientCall
                .SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {

                openTelemetry.getPropagators()
                        .getTextMapPropagator()
                        .inject(
                                Context.current(),
                                headers,
                                new GrpcMetadataSetter()
                        );

                super.start(responseListener, headers);
            }
        };
    }
}

