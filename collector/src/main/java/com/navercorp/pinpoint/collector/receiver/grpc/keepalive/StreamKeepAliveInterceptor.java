package com.navercorp.pinpoint.collector.receiver.grpc.keepalive;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Closeable;
import java.util.Objects;

public class StreamKeepAliveInterceptor implements ServerInterceptor, Closeable {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final KeepAliveRegistry registry;
    private final String name;

    public StreamKeepAliveInterceptor(String name, KeepAliveRegistry registry) {
        this.name = Objects.requireNonNull(name, "name");
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        final ServerCall.Listener<ReqT> listener = next.startCall(call, headers);

        logger.debug("{} KeepAliveInterceptor.interceptCall", name);

        LastPacketTimeCapture updater = new LastPacketTimeCapture();
        registry.put(call, updater);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(listener) {

            @Override
            public void onMessage(ReqT message) {
                logger.debug("onMessage update last packet");
                updater.update();
                delegate().onMessage(message);
            }

            @Override
            public void onCancel() {
                logger.debug("onCancel remove ServerCall");
                registry.remove(call);
                delegate().onCancel();
            }

            @Override
            public void onComplete() {
                logger.debug("onComplete remove ServerCall");
                registry.remove(call);
                delegate().onCancel();
            }
        };

    }

    @Override
    public void close() {
        logger.info("close registry");
        this.registry.close();
    }

    @Override
    public String toString() {
        return "StreamKeepAliveInterceptor{" +
                "name='" + name + '\'' +
                ", registry=" + registry +
                '}';
    }
}
