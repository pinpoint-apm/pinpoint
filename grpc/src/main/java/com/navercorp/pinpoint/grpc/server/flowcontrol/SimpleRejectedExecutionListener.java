package com.navercorp.pinpoint.grpc.server.flowcontrol;

import io.grpc.Metadata;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public class SimpleRejectedExecutionListener implements RejectedExecutionListener {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final Status STREAM_IDLE_TIMEOUT = Status.DEADLINE_EXCEEDED.withDescription("Stream idle timeout");

    private final ServerCallWrapper serverCall;
    private final IdleTimeout idleTimeout;

    public SimpleRejectedExecutionListener(ServerCallWrapper serverCall, IdleTimeout idleTimeout) {
        this.serverCall = Objects.requireNonNull(serverCall, "serverCall");
        this.idleTimeout = Objects.requireNonNull(idleTimeout, "idleTimeout");
    }

    @Override
    public void onRejectedExecution() {
        // Request immediately
        this.serverCall.request(1);
    }

    @Override
    public void onSchedule() {
        // empty
    }

    @Override
    public long getRejectedExecutionCount() {
        return 0;
    }

    @Override
    public void onMessage() {
        this.idleTimeout.update();
    }

    @Override
    public boolean idleTimeExpired() {
        return this.idleTimeout.isExpired();
    }

    @Override
    public void idleTimeout() {
        logger.info("stream idle timeout applicationName:{} agentId:{}", serverCall.getApplicationName(), serverCall.getAgentId());
        serverCall.cancel(STREAM_IDLE_TIMEOUT, new Metadata());
    }

    @Override
    public String toString() {
        return "SimpleRejectedExecutionListener{" +
                "serverCall=" + serverCall +
                '}';
    }
}
