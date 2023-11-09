package com.navercorp.pinpoint.profiler.sender.grpc;

import java.util.Objects;
import com.navercorp.pinpoint.profiler.sender.grpc.stream.StreamJob;
import io.grpc.stub.ClientCallStreamObserver;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.util.concurrent.Future;

public class DefaultStreamEventListener<ReqT> implements StreamEventListener<ReqT> {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final Reconnector reconnector;
    private final StreamJob<ReqT> streamJob;
    private volatile Future<?> handle;
    private boolean aborted;
    private boolean completed;

    public DefaultStreamEventListener(Reconnector reconnector, StreamJob<ReqT> streamJob) {
        this.reconnector = Objects.requireNonNull(reconnector, "reconnector");
        this.streamJob = Objects.requireNonNull(streamJob, "streamTask");
    }

    @Override
    public void start(final ClientCallStreamObserver<ReqT> requestStream) {
        if (aborted) {
            logger.info("stream terminated");
            return;
        }
        if (completed) {
            logger.info("stream completed");
            return;
        }
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
        aborted = true;
    }

    @Override
    public void onCompleted() {
        final Future<?> handle = this.handle;
        if (handle != null) {
            handle.cancel(true);
        }
        reconnector.reconnect();
        completed = true;
    }

    @Override
    public String toString() {
        return "DefaultStreamEventListener{" +
                streamJob +
                '}';
    }
}
