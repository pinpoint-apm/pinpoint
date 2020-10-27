package com.navercorp.pinpoint.profiler.sender.grpc;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.sender.grpc.stream.StreamJob;
import io.grpc.stub.ClientCallStreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

public class DefaultStreamEventListener<ReqT> implements StreamEventListener<ReqT> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Reconnector reconnector;
    private final StreamJob<ReqT> streamJob;
    private volatile Future<?> handle;


    public DefaultStreamEventListener(Reconnector reconnector, StreamJob<ReqT> streamJob) {
        this.reconnector = Assert.requireNonNull(reconnector, "reconnector");
        this.streamJob = Assert.requireNonNull(streamJob, "streamTask");
    }

    @Override
    public void start(final ClientCallStreamObserver<ReqT> requestStream) {
        this.handle = streamJob.start(requestStream);
        reconnector.reset();
    }


    @Override
    public void onError(Throwable t) {
        final Future<?> handle = this.handle;
        if (handle != null) {
            handle.cancel(true);
        }
        reconnector.reconnect();
    }

    @Override
    public void onCompleted() {
        final Future<?> handle = this.handle;
        if (handle != null) {
            handle.cancel(true);
        }
        reconnector.reconnect();
    }

    @Override
    public String toString() {
        return "DefaultStreamEventListener{" +
                streamJob +
                '}';
    }
}
