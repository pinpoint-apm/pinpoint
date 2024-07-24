package com.navercorp.pinpoint.grpc.server;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

import java.util.concurrent.atomic.AtomicLong;

public class StreamCountInterceptor implements ServerInterceptor {
    private final AtomicLong currentStream = new AtomicLong();
    public StreamCountInterceptor() {
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        currentStream.incrementAndGet();
        ServerCall.Listener<ReqT> listener = next.startCall(call, headers);
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
            @Override
            public void onCancel() {
                currentStream.decrementAndGet();
                super.onCancel();
            }

            @Override
            public void onComplete() {
                currentStream.decrementAndGet();
                super.onComplete();
            }

        };
    }

    public long getCurrentStream() {
        return currentStream.get();
    }
}
