package com.navercorp.pinpoint.profiler.sender.grpc.stream;

import io.grpc.stub.ClientCallStreamObserver;

import java.util.concurrent.Future;

public interface StreamJob<ReqT> {

    Future<?> start(final ClientCallStreamObserver<ReqT> requestStream);

}
