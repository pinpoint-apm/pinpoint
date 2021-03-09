package com.navercorp.pinpoint.grpc.server.flowcontrol;

public interface RejectedExecutionListener {
    void onRejectedExecution();

    void onSchedule();

    long getRejectedExecutionCount();

    void onExecute();
}
