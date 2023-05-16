package com.navercorp.pinpoint.grpc.server.flowcontrol;

import io.grpc.Metadata;
import io.grpc.Status;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Objects;
import java.util.concurrent.Future;

public class SimpleRejectedExecutionListener implements RejectedExecutionListener {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final Status STREAM_IDLE_TIMEOUT = Status.DEADLINE_EXCEEDED.withDescription("Stream idle timeout");

    private final String name;
    private final ServerCallWrapper serverCall;
    private final IdleTimeout idleTimeout;

    private volatile Future<?> future;

    public SimpleRejectedExecutionListener(String name, ServerCallWrapper serverCall, IdleTimeout idleTimeout) {
        this.name = Objects.requireNonNull(name, "name");
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

    private boolean idleTimeExpired() {
        return this.idleTimeout.isExpired();
    }


    private void idleTimeout() {
        logger.info("stream idle timeout applicationName:{} agentId:{} {}", serverCall.getApplicationName(), serverCall.getAgentId(), this.name);
        serverCall.cancel(STREAM_IDLE_TIMEOUT, new Metadata());
    }

    @Override
    public void setFuture(Future<?> future) {
        this.future = Objects.requireNonNull(future, "future");
    }

    @Override
    public boolean cancel() {
        final Future<?> future = this.future;
        if (future == null) {
            return false;
        }
        return future.cancel(false);
    }

    @Override
    public boolean isCancelled() {
        final Future<?> future = this.future;
        if (future == null) {
            return false;
        }
        return future.isCancelled();
    }

    @Override
    public String toString() {
        return "SimpleRejectedExecutionListener{" +
                "serverCall=" + serverCall +
                '}';
    }
}
