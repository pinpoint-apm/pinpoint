package com.navercorp.pinpoint.grpc.server.flowcontrol;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class ControlFlowRejectExecutionListener implements RejectedExecutionListener {
    private final AtomicLong rejectedExecutionCounter = new AtomicLong(0);
    private final StreamExecutorRejectedExecutionRequestScheduler.ServerCallWrapper serverCall;
    private final long recoveryMessagesCount;

    public ControlFlowRejectExecutionListener(StreamExecutorRejectedExecutionRequestScheduler.ServerCallWrapper serverCall, long recoveryMessagesCount) {
        this.serverCall = Objects.requireNonNull(serverCall, "serverCall");
        this.recoveryMessagesCount = recoveryMessagesCount;
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
    public void onExecute() {

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