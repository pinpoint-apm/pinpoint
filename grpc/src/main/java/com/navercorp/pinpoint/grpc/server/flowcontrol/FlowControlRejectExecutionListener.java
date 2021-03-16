package com.navercorp.pinpoint.grpc.server.flowcontrol;

import io.grpc.Metadata;
import io.grpc.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class FlowControlRejectExecutionListener implements RejectedExecutionListener {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final Status STREAM_IDLE_TIMEOUT = Status.DEADLINE_EXCEEDED.withDescription("Stream idle timeout");

    private final AtomicLong rejectedExecutionCounter = new AtomicLong(0);
    private final ServerCallWrapper serverCall;
    private final long recoveryMessagesCount;

    private final IdleTimeout idleTimeout;

    public FlowControlRejectExecutionListener(ServerCallWrapper serverCall, long recoveryMessagesCount, IdleTimeout idleTimeout) {
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
        final StringBuilder sb = new StringBuilder("RejectedExecutionListener{");
        sb.append("rejectedExecutionCounter=").append(rejectedExecutionCounter);
        sb.append(", serverCall=").append(serverCall);
        sb.append('}');
        return sb.toString();
    }
}