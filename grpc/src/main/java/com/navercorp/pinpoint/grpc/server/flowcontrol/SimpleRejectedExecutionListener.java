package com.navercorp.pinpoint.grpc.server.flowcontrol;

import java.util.Objects;

public class SimpleRejectedExecutionListener implements RejectedExecutionListener {

    private final StreamExecutorRejectedExecutionRequestScheduler.ServerCallWrapper serverCall;

    public SimpleRejectedExecutionListener(StreamExecutorRejectedExecutionRequestScheduler.ServerCallWrapper serverCall) {
        this.serverCall = Objects.requireNonNull(serverCall, "serverCall");
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
    public void onExecute() {
        // empty
    }

    @Override
    public String toString() {
        return "SimpleRejectedExecutionListener{" +
                "serverCall=" + serverCall +
                '}';
    }
}
