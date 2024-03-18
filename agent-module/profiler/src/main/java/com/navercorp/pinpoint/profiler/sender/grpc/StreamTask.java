package com.navercorp.pinpoint.profiler.sender.grpc;

public interface StreamTask<M, ReqT> {

    void start();

    void stop();
}
