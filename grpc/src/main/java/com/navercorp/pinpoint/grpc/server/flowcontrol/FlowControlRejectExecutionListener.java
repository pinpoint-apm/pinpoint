package com.navercorp.pinpoint.grpc.server.flowcontrol;

import io.grpc.Metadata;
import io.grpc.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

public class FlowControlRejectExecutionListener implements RejectedExecutionListener {

    private static final AtomicLongFieldUpdater<FlowControlRejectExecutionListener> REJECT =
            AtomicLongFieldUpdater.newUpdater(FlowControlRejectExecutionListener.class, "rejectedExecutionCounter");

    private static final Status STREAM_IDLE_TIMEOUT = Status.DEADLINE_EXCEEDED.withDescription("Stream idle timeout");

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final String name;

    private volatile long rejectedExecutionCounter;

    private final ServerCallWrapper serverCall;
    private final long recoveryMessagesCount;

    private final IdleTimeout idleTimeout;

    private volatile Future<?> future;

    public FlowControlRejectExecutionListener(String name, ServerCallWrapper serverCall, long recoveryMessagesCount, IdleTimeout idleTimeout) {
        this.name = Objects.requireNonNull(name, "name");
        this.serverCall = Objects.requireNonNull(serverCall, "serverCall");
        this.recoveryMessagesCount = recoveryMessagesCount;
        this.idleTimeout = Objects.requireNonNull(idleTimeout, "idleTimeout");
    }

    @Override
    public void onRejectedExecution() {
        REJECT.incrementAndGet(this);
    }

    @Override
    public void onSchedule() {
        if (logger.isTraceEnabled()) {
            logger.trace("Stream state check {} agent:{}/{}", this.name, serverCall.getApplicationName(), serverCall.getAgentId());
        }
        if (this.serverCall.isCancelled()) {
            logger.info("Stream already cancelled:{} agent:{}/{}", this.name, serverCall.getApplicationName(), serverCall.getAgentId());
            this.cancel();
            return;
        }
        if (!expireIdleTimeout()) {
            reject();
        }
    }

    private boolean expireIdleTimeout() {
        if (this.idleTimeExpired()) {
            if (this.cancel()) {
                this.idleTimeout();
                return true;
            }
        }
        return false;
    }


    private void reject() {
        final long currentRejectCount = getRejectedExecutionCount();
        if (currentRejectCount > 0) {
            final long recovery = Math.min(currentRejectCount, recoveryMessagesCount);
            REJECT.addAndGet(this, -recovery);
            if (logger.isDebugEnabled()) {
                logger.debug("flow-control request:{} {}/{}", recovery, serverCall.getApplicationName(), serverCall.getAgentId());
            }
            serverCall.request((int) recovery);
        }
    }

    @Override
    public long getRejectedExecutionCount() {
        return REJECT.get(this);
    }

    @Override
    public void onMessage() {
        this.idleTimeout.update();
    }

    private boolean idleTimeExpired() {
        return this.idleTimeout.isExpired();
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


    private void idleTimeout() {
        logger.info("Stream idle timeout agent:{}/{} {}", this.name, serverCall.getApplicationName(), serverCall.getAgentId());
        try {
            serverCall.cancel(STREAM_IDLE_TIMEOUT, new Metadata());
        } catch (IllegalStateException ex) {
            logger.warn("Failed to cancel stream. agent:{}/{} {}", serverCall.getApplicationName(), serverCall.getAgentId(), ex.getMessage());
        }
    }

    @Override
    public String toString() {
        return "RejectedExecutionListener{" +
                "rejectedExecutionCounter=" + rejectedExecutionCounter +
                ", serverCall=" + serverCall +
                '}';
    }
}