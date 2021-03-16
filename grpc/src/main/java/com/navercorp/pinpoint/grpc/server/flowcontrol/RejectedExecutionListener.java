package com.navercorp.pinpoint.grpc.server.flowcontrol;

public interface RejectedExecutionListener {
    void onRejectedExecution();

    void onSchedule();

    long getRejectedExecutionCount();

    void onMessage();

    void idleTimeout();

    boolean idleTimeExpired();
}
