package com.navercorp.pinpoint.grpc.stream;

import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class ClientCallStateStreamObserver<ReqT> extends ClientCallStreamObserver<ReqT>{

    @SuppressWarnings("rawtypes")
    private static final AtomicReferenceFieldUpdater<ClientCallStateStreamObserver, ObserverState> STATE
            = AtomicReferenceFieldUpdater.newUpdater(ClientCallStateStreamObserver.class, ObserverState.class, "state");

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ClientCallStreamObserver<ReqT> delegate;

    private volatile ObserverState state = ObserverState.RUN;

    public static <ReqT> ClientCallStateStreamObserver<ReqT> clientCall(StreamObserver<ReqT> delegate) {
        if (delegate instanceof ClientCallStreamObserver) {
            ClientCallStreamObserver<ReqT> clientCall = (ClientCallStreamObserver<ReqT>) delegate;
            return new ClientCallStateStreamObserver<>(clientCall);
        }
        throw new IllegalArgumentException("delegate is not instance of ClientCallStreamObserver");
    }

    public ClientCallStateStreamObserver(ClientCallStreamObserver<ReqT> delegate) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
    }

    ClientCallStreamObserver<ReqT> delegate() {
        return delegate;
    }

    @Override
    public void cancel(@Nullable String message, @Nullable Throwable cause) {
        delegate().cancel(message, cause);
    }

    @Override
    public void disableAutoRequestWithInitial(int request) {
        delegate().disableAutoRequestWithInitial(request);
    }

    @Override
    public boolean isReady() {
        return delegate().isReady();
    }

    @Override
    public void setOnReadyHandler(Runnable onReadyHandler) {
        delegate().setOnReadyHandler(onReadyHandler);
    }

    @Override
    public void request(int count) {
        delegate().request(count);
    }

    @Override
    public void setMessageCompression(boolean enable) {
        delegate().setMessageCompression(enable);
    }

    @Override
    public void disableAutoInboundFlowControl() {
        delegate().disableAutoInboundFlowControl();
    }

    @Override
    public void onNext(ReqT value) {
        delegate().onNext(value);
    }

    @Override
    public void onError(Throwable t) {
        if (ObserverState.changeError(STATE, this)) {
            delegate().onError(t);
        } else {
            // for debugging
            logger.warn("onError() WARNING. state already changed {}", state);
        }
    }

    @Override
    public void onCompleted() {
        if (ObserverState.changeComplete(STATE, this)) {
            delegate().onCompleted();
        } else {
            // for debugging
            logger.warn("onComplete() WARNING. state already changed {}", state);
        }
    }

    public boolean isRun() {
        return state.isRun();
    }

    public boolean isClosed() {
        return state.isClosed();
    }

    public ObserverState state() {
        return state;
    }

    @Override
    public String toString() {
        return "ClientCallStateStreamObserver{" +
                "delegate=" + delegate +
                ", state=" + state +
                '}';
    }
}
