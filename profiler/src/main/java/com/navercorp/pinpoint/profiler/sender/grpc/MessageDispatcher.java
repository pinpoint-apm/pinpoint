package com.navercorp.pinpoint.profiler.sender.grpc;

import io.grpc.stub.ClientCallStreamObserver;

public interface MessageDispatcher<ReqT> {
    void onDispatch(ClientCallStreamObserver<ReqT> stream, Object message);
}
