package com.navercorp.pinpoint.profiler.sender.grpc;

import com.navercorp.pinpoint.grpc.stream.ClientCallStateStreamObserver;

public interface StreamEventListener<ReqT> {
    void start(ClientCallStateStreamObserver<ReqT> requestStream);

    void onError(Throwable t);

    void onCompleted();
}
