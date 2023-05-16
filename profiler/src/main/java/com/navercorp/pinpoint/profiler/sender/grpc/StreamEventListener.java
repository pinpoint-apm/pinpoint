package com.navercorp.pinpoint.profiler.sender.grpc;

import io.grpc.stub.ClientCallStreamObserver;

public interface StreamEventListener<ReqT> {
    void start(ClientCallStreamObserver<ReqT> requestStream);

    void onError(Throwable t);

    void onCompleted();
}
