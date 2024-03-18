package com.navercorp.pinpoint.profiler.sender.grpc;

public interface StreamState {
    void fail();

    boolean isFailure();

    void success();
}
