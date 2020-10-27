package com.navercorp.pinpoint.profiler.sender.grpc;

public interface StreamTask<ReqT> {

    void start();

    void stop();
}
