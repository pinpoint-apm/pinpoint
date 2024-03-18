package com.navercorp.pinpoint.profiler.sender.grpc.stream;

import com.navercorp.pinpoint.profiler.sender.grpc.ResponseStreamObserver;
import io.grpc.stub.ClientCallStreamObserver;

public interface ClientStreamingProvider<ReqT, ResT> {
    ClientCallStreamObserver<ReqT> newStream(ResponseStreamObserver<ReqT, ResT> response);
}
