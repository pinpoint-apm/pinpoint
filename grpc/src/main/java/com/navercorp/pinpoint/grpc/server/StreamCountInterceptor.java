package com.navercorp.pinpoint.grpc.server;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
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

        MethodDescriptor.MethodType type = call.getMethodDescriptor().getType();
        if (MethodDescriptor.MethodType.UNARY == type) {
            return next.startCall(call, headers);
        }

        currentStream.incrementAndGet();
        final Supplier<Void> streamClose = Suppliers.memoize(() -> {
            currentStream.decrementAndGet();
            return null;
        });

        ServerCall.Listener<ReqT> listener = next.startCall(call, headers);
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
            @Override
            public void onCancel() {
                streamClose.get();
                super.onCancel();
            }

            @Override
            public void onComplete() {
                streamClose.get();
                super.onComplete();
            }

        };
    }

    public long getCurrentStream() {
        return currentStream.get();
    }
}
