package com.navercorp.pinpoint.profiler.sender.grpc;

public interface StreamState {
    void fail();

    long getFailCount();

    boolean isFailure();

    void success();
}
