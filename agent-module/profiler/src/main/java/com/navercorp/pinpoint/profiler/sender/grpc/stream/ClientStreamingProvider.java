package com.navercorp.pinpoint.profiler.sender.grpc.stream;

import com.navercorp.pinpoint.grpc.stream.ClientCallStateStreamObserver;
import com.navercorp.pinpoint.profiler.sender.grpc.ResponseStreamObserver;

public interface ClientStreamingProvider<ReqT, ResT> {
    ClientCallStateStreamObserver<ReqT> newStream(ResponseStreamObserver<ReqT, ResT> response);
}
