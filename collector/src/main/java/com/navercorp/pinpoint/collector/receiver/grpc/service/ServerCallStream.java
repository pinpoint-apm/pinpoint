package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.google.protobuf.GeneratedMessageV3;
import com.navercorp.pinpoint.grpc.Header;
import com.navercorp.pinpoint.grpc.server.ServerContext;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.StatusException;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.function.Supplier;

public class ServerCallStream<Req extends GeneratedMessageV3, Res extends GeneratedMessageV3> implements StreamObserver<Req> {
    private final Logger logger;

    @SuppressWarnings("rawtypes")
    private static final AtomicIntegerFieldUpdater<ServerCallStream> UPDATER = AtomicIntegerFieldUpdater.newUpdater(ServerCallStream.class, "handleErrorState");
    static final int HANDLE_ERROR_STATE_INIT = 0;
    static final int HANDLE_ERROR_STATE_COMPLETED = 1;

    private volatile int handleErrorState;

    private final long streamId;

    private final ServerCallStreamObserver<Res> responseObserver;
    private final StreamCloseOnError streamCloseOnError;
    private final Supplier<Res> responseSupplier;

    private final ServerStreamDispatch<Req, Res> dispatch;

    public ServerCallStream(Logger logger,
                            long streamId,
                            ServerCallStreamObserver<Res> responseObserver,
                            ServerStreamDispatch<Req, Res> dispatch,
                            StreamCloseOnError streamCloseOnError,
                            Supplier<Res> responseSupplier) {
        this.logger = Objects.requireNonNull(logger, "logger");
        this.streamId = streamId;
        this.responseObserver = Objects.requireNonNull(responseObserver, "responseObserver");
        this.dispatch = Objects.requireNonNull(dispatch, "dispatch");

        this.streamCloseOnError = Objects.requireNonNull(streamCloseOnError, "streamCloseOnError");
        this.responseSupplier = Objects.requireNonNull(responseSupplier, "responseSupplier");
    }

    @Override
    public void onNext(Req req) {
        dispatch.onNext(req, this);
    }

    @Override
    public void onError(Throwable throwable) {
        Status status = Status.fromThrowable(throwable);
        Metadata metadata = Status.trailersFromThrowable(throwable);
        if (logger.isInfoEnabled()) {
            Header header = ServerContext.getAgentInfo();
            logger.info("onError: Failed to span streamId=, {} {} {}", streamId, header, status, metadata);
        }

        responseCompleted();
    }

    @Override
    public void onCompleted() {
        if (logger.isInfoEnabled()) {
            Header header = ServerContext.getAgentInfo();
            logger.info("onCompleted streamId={} {}", streamId, header);
        }

        responseCompleted();
    }

    private void responseCompleted() {
        if (responseObserver.isCancelled()) {
            logger.info("responseCompleted: ResponseObserver is cancelled streamId={}", streamId);
            return;
        }
        Res response = responseSupplier.get();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    int getHandleErrorState() {
        return UPDATER.get(this);
    }

    public void onNextError(Throwable e) {
        if (!UPDATER.compareAndSet(this, HANDLE_ERROR_STATE_INIT, HANDLE_ERROR_STATE_COMPLETED)) {
            logger.info("handleError: handleError already finished streamId={}", streamId);
            return;
        }
        if (!streamCloseOnError.onError(e)) {
            return;
        }
        if (responseObserver.isCancelled()) {
            logger.info("handleError: ResponseObserver is cancelled streamId={}", streamId);
            return;
        }
        if (e instanceof StatusException || e instanceof StatusRuntimeException) {
            responseObserver.onError(e);
        } else {
            // Avoid detailed exception
            StatusException statusException = Status.INTERNAL.withDescription("Bad Request").asException();
            responseObserver.onError(statusException);
        }
    }

}
