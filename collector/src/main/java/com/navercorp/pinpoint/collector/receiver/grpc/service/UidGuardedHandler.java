package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.navercorp.pinpoint.collector.handler.SimpleHandler;
import com.navercorp.pinpoint.common.profiler.logging.ThrottledLogger;
import com.navercorp.pinpoint.common.server.io.ServerRequest;
import com.navercorp.pinpoint.io.request.UidNotFoundException;
import org.apache.logging.log4j.LogManager;

import java.util.Objects;

/**
 * The single net for the "service not found" discard policy: data of an unregistered service is
 * discarded without failing the stream (a closed stream would make the agent reconnect in a
 * tight loop), and the agent learns about the unregistered service from the agentInfo response.
 */
public class UidGuardedHandler<T> implements SimpleHandler<T> {

    private final SimpleHandler<T> delegate;
    private final ThrottledLogger uidLogger;

    public UidGuardedHandler(SimpleHandler<T> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.uidLogger = ServiceNotFoundChecker.newUidLogger(LogManager.getLogger(delegate.getClass()));
    }

    @Override
    public void handleSimple(ServerRequest<T> request) {
        try {
            delegate.handleSimple(request);
        } catch (UidNotFoundException e) {
            uidLogger.warn("Service not found. Discarding {}. header={}", request.getMessageType(), request.getHeader());
        }
    }

    @Override
    public String toString() {
        return "UidGuardedHandler{" + delegate + '}';
    }
}