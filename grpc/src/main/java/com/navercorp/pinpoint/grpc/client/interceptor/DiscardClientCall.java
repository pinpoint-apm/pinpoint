/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.grpc.client.interceptor;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.grpc.client.ForwardClientCall;
import io.grpc.ClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Woonduk Kang(emeroad)
 * @author jaehong.kim
 */
class DiscardClientCall<ReqT, RespT> extends ForwardClientCall<ReqT, RespT> {
    private static final State OK = new State(false, "OK");
    private static final State NOT_READY = new State(false, "stream not ready");

    private final AtomicBoolean onReadyState = new AtomicBoolean(false);
    private final AtomicLong pendingCounter = new AtomicLong();
    private final long maxPendingThreshold;
    private final DiscardEventListener listener;
    private final DiscardLimiter discardLimiter;

    public DiscardClientCall(ClientCall<ReqT, RespT> delegate, DiscardEventListener listener, long maxPendingThreshold, long discardCountForReconnect, long notReadyTimeoutMillis) {
        super(delegate);
        this.listener = Assert.requireNonNull(listener, "listener");
        this.maxPendingThreshold = maxPendingThreshold;
        this.discardLimiter = new DiscardLimiter(discardCountForReconnect, notReadyTimeoutMillis);
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
        this.discardLimiter.reset();
    }

    @Override
    public void sendMessage(ReqT message) {
        final State state = readyState();
        if (OK == state) {
            super.sendMessage(message);
        } else if (Boolean.FALSE == state.isCancel()) {
            discardMessage(message, state.getMessage());
        } else {
            cancel(state.getMessage(), state.getCause());
        }
    }

    private State readyState() {
        // skip pending queue cancel : DelayedStream
        if (Boolean.FALSE == this.onReadyState.get()) {
            final long pendingCount = this.pendingCounter.incrementAndGet();
            if (pendingCount > this.maxPendingThreshold) {
                final String message = "maximum pending requests " + pendingCount + "/" + this.maxPendingThreshold;
                return new State(false, message);
            }
            return OK;
        }

        final boolean ready = isReady();
        try {
            this.discardLimiter.discard(ready);
        } catch (DiscardLimiter.DiscardLimiterException e) {
            return new State(true, e.getMessage(), e);
        }

        if (Boolean.FALSE == ready) {
            return NOT_READY;
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
        private final boolean cancel;
        private final String message;
        private final Throwable cause;

        public State(boolean cancel, String message) {
            this(cancel, message, null);
        }

        public State(boolean cancel, String message, Throwable cause) {
            this.cancel = cancel;
            this.message = message;
            this.cause = cause;
        }

        public boolean isCancel() {
            return cancel;
        }

        public String getMessage() {
            return message;
        }

        public Throwable getCause() {
            return cause;
        }
    }
}