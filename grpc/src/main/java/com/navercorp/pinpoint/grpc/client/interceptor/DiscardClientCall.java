package com.navercorp.pinpoint.grpc.client.interceptor;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.client.ForwardClientCall;
import io.grpc.ClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

class DiscardClientCall<ReqT, RespT> extends ForwardClientCall<ReqT, RespT> {
    private static final State OK = new State(true, "OK");
    private final AtomicBoolean onReadyState = new AtomicBoolean(false);
    private final AtomicLong pendingCounter = new AtomicLong();
    private final long maxPendingThreshold;
    private final DiscardEventListener listener;

    public DiscardClientCall(ClientCall<ReqT, RespT> delegate, DiscardEventListener listener, long maxPendingThreshold) {
        super(delegate);
        this.listener = Assert.requireNonNull(listener, "listener");
        this.maxPendingThreshold = maxPendingThreshold;
    }

    @Override
    public void start(Listener<RespT> responseListener, Metadata headers) {
        ClientCall.Listener<RespT> onReadyListener = new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
            @Override
            public void onReady() {
                DiscardClientCall.this.reset();
                super.onReady();
            }
        };
        super.start(onReadyListener, headers);
    }

    private void reset() {
        this.onReadyState.compareAndSet(false, true);
        this.pendingCounter.set(0);
    }

    @Override
    public void sendMessage(ReqT message) {
        final State state = readyState();
        if (OK == state) {
            super.sendMessage(message);
        } else {
            discardMessage(message, state.getCause());
        }
    }

    private State readyState() {
        // skip pending queue state : DelayedStream
        if (this.onReadyState.get() == false) {
            final long pendingCount = this.pendingCounter.incrementAndGet();
            if (pendingCount > this.maxPendingThreshold) {
                final String cause = "maximum pending requests " + pendingCount + "/" + this.maxPendingThreshold;
                return new State(false, cause);
            }
            return OK;
        }

        if (isReady() == false) {
            return new State(false, "stream not ready");
        }
        return OK;
    }

    private void discardMessage(ReqT message, String cause) {
        this.listener.onDiscard(message, cause);
    }

    @Override
    public void cancel(String message, Throwable cause) {
        this.listener.onCancel(message, cause);
        super.cancel(message, cause);
    }

    public boolean getOnReadyState() {
        return onReadyState.get();
    }


    public long getPendingCount() {
        return pendingCounter.get();
    }

    private static class State {
        private final boolean state;
        private final String cause;

        public State(boolean state, String cause) {
            this.state = state;
            this.cause = cause;
        }

        public String getCause() {
            return cause;
        }
    }

}