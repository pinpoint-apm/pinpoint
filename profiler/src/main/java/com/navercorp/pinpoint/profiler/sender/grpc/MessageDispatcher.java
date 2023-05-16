package com.navercorp.pinpoint.profiler.sender.grpc;

import io.grpc.stub.ClientCallStreamObserver;

public interface MessageDispatcher<M, ReqT> {
    void onDispatch(ClientCallStreamObserver<ReqT> stream, M message);
}
