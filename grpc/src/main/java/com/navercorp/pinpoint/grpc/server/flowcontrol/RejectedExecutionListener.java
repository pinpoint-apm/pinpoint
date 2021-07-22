package com.navercorp.pinpoint.grpc.server.flowcontrol;

import java.util.concurrent.Future;

public interface RejectedExecutionListener {
    void onRejectedExecution();

    void onSchedule();

    long getRejectedExecutionCount();

    void onMessage();

    void setFuture(Future<?> future);

    boolean cancel();

    boolean isCancelled();
}
