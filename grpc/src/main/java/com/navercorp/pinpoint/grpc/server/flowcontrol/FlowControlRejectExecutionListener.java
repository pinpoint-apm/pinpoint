package com.navercorp.pinpoint.grpc.server.flowcontrol;

import io.grpc.Metadata;
import io.grpc.Status;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.Objects;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

public class FlowControlRejectExecutionListener implements RejectedExecutionListener {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final Status STREAM_IDLE_TIMEOUT = Status.DEADLINE_EXCEEDED.withDescription("Stream idle timeout");

    private final String name;

    private final AtomicLong rejectedExecutionCounter = new AtomicLong(0);
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
        this.rejectedExecutionCounter.incrementAndGet();
    }

    @Override
    public void onSchedule() {
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
        final long currentRejectCount = this.rejectedExecutionCounter.get();
        if (currentRejectCount > 0) {
            final long recovery = Math.min(currentRejectCount, recoveryMessagesCount);
            this.rejectedExecutionCounter.addAndGet(-recovery);
            serverCall.request((int) recovery);
        }
    }

    @Override
    public long getRejectedExecutionCount() {
        return rejectedExecutionCounter.get();
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
        logger.info("stream idle timeout applicationName:{} agentId:{} {}", this.name, serverCall.getApplicationName(), serverCall.getAgentId());
        serverCall.cancel(STREAM_IDLE_TIMEOUT, new Metadata());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RejectedExecutionListener{");
        sb.append("rejectedExecutionCounter=").append(rejectedExecutionCounter);
        sb.append(", serverCall=").append(serverCall);
        sb.append('}');
        return sb.toString();
    }
}