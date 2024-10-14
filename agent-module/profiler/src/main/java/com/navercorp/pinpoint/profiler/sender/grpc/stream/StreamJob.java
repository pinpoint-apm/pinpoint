package com.navercorp.pinpoint.profiler.sender.grpc.stream;

import com.navercorp.pinpoint.grpc.stream.ClientCallStateStreamObserver;

import java.util.concurrent.Future;

public interface StreamJob<ReqT> {

    Future<?> start(final ClientCallStateStreamObserver<ReqT> requestStream);

}
