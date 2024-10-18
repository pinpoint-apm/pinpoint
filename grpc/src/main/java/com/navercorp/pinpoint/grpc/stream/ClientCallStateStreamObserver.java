package com.navercorp.pinpoint.grpc.stream;

import io.grpc.stub.ClientCallStreamObserver;
import io.grpc.stub.StreamObserver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Objects;

public class ClientCallStateStreamObserver<ReqT> extends ClientCallStreamObserver<ReqT>{

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ClientCallStreamObserver<ReqT> delegate;

    private final ClientCallContext context;

    public static <ReqT> ClientCallStateStreamObserver<ReqT> clientCall(StreamObserver<ReqT> delegate, ClientCallContext context) {
        if (delegate instanceof ClientCallStreamObserver) {
            ClientCallStreamObserver<ReqT> clientCall = (ClientCallStreamObserver<ReqT>) delegate;
            return new ClientCallStateStreamObserver<>(clientCall, context);
        }
        throw new IllegalArgumentException("delegate is not instance of ClientCallStreamObserver");
    }

    public ClientCallStateStreamObserver(ClientCallStreamObserver<ReqT> delegate, ClientCallContext context) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.context = Objects.requireNonNull(context, "context");
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
        if (requestState().onErrorState()) {
            delegate().onError(t);
        } else {
            // for debugging
            logger.warn("onError() WARNING. state already changed {}", state());
        }
    }

    @Override
    public void onCompleted() {
        if (requestState().onCompleteState()) {
            delegate().onCompleted();
        } else {
            // for debugging
            logger.warn("onComplete() WARNING. state already changed {}", state());
        }
    }

    public boolean isRun() {
        return requestState().isRun();
    }

    public boolean isClosed() {
        return requestState().isClosed();
    }

    public StreamState state() {
        return requestState();
    }

    private StreamState requestState() {
        return context.request();
    }

    @Override
    public String toString() {
        return "ClientCallStateStreamObserver{" +
                "delegate=" + delegate +
                ", " + context +
                '}';
    }
}
