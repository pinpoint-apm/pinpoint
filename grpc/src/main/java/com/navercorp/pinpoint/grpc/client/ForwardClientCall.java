package com.navercorp.pinpoint.grpc.client;

import io.grpc.Attributes;
import io.grpc.ClientCall;
import io.grpc.Metadata;


/**
 * io.grpc.ForwardingClientCall <- compiler bug issue
 * https://github.com/naver/pinpoint/issues/7050
 */
public class ForwardClientCall<ReqT, RespT> extends ClientCall<ReqT, RespT> {

    private final ClientCall<ReqT, RespT> delegate;

    public ForwardClientCall(ClientCall<ReqT, RespT> delegate) {
        this.delegate = delegate;
    }

    public ClientCall<ReqT, RespT> delegate() {
        return delegate;
    }

    @Override
    public void start(Listener<RespT> responseListener, Metadata headers) {
        delegate().start(responseListener, headers);
    }

    @Override
    public void request(int numMessages) {
        delegate().request(numMessages);
    }

    @Override
    public void cancel(String message, Throwable cause) {
        delegate().cancel(message, cause);
    }

    @Override
    public void halfClose() {
        delegate().halfClose();
    }

    @Override
    public void sendMessage(ReqT message) {
        delegate().sendMessage(message);
    }

    @Override
    public boolean isReady() {
        return delegate().isReady();
    }

    @Override
    public void setMessageCompression(boolean enabled) {
        delegate().setMessageCompression(enabled);
    }

    @Override
    public Attributes getAttributes() {
        return delegate().getAttributes();
    }

    @Override
    public String toString() {
        return "ForwardClientCall{" +
                "delegate=" + delegate +
                '}';
    }
}
