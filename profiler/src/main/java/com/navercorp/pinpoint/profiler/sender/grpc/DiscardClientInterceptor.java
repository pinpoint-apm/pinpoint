/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.sender.grpc;

import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.util.Assert;
import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.ForwardingClientCallListener;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import org.slf4j.Logger;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DiscardClientInterceptor implements ClientInterceptor {

    private final Logger logger;
    private final long rateLimitCount;
    private final long maxPendingThreshold;

    public DiscardClientInterceptor(Logger logger, long rateLimitCount, long maxPendingThreshold) {
        this.logger = Assert.requireNonNull(logger, "logger");
        this.rateLimitCount = rateLimitCount;
        this.maxPendingThreshold = maxPendingThreshold;
    }

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        final ClientCall<ReqT, RespT> newCall = next.newCall(method, callOptions);
        return new DiscardClientCall<ReqT, RespT>(newCall, maxPendingThreshold);
    }

    public class DiscardClientCall<ReqT, RespT> extends ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT> {

        private final AtomicBoolean onReadyState = new AtomicBoolean(false);
        private final AtomicLong pendingCounter = new AtomicLong();
        private final long maxPendingThreshold;
        private final AtomicLong discardCounter = new AtomicLong();

        public DiscardClientCall(io.grpc.ClientCall<ReqT, RespT> delegate, long maxPendingThreshold) {
            super(delegate);
            this.maxPendingThreshold = maxPendingThreshold;
        }

        @Override
        public void start(Listener<RespT> responseListener, Metadata headers) {
            ClientCall.Listener<RespT> onReadyListener = new ForwardingClientCallListener.SimpleForwardingClientCallListener<RespT>(responseListener) {
                @Override
                public void onReady() {
                    DiscardClientCall.this.onReadyState.compareAndSet(false, true);
                    super.onReady();
                }
            };
            super.start(onReadyListener, headers);
        }

        @Override
        public void sendMessage(ReqT message) {
            if (readyState()) {
                super.sendMessage(message);
            } else {
                discardMessage(message);
            }
        }

        private void discardMessage(ReqT message) {
            final long beforeDiscardCount = this.discardCounter.getAndIncrement();
            if ((beforeDiscardCount % DiscardClientInterceptor.this.rateLimitCount) == 0) {
                discardLog(message, beforeDiscardCount + 1);
            }
        }

        private void discardLog(ReqT message, long discardCount) {
            logger.info("Discard {} message, stream not ready. discardCount:{}", getMessageType(message), discardCount);
        }

        private String getMessageType(ReqT message) {
            if (message == null) {
                return "null";
            }
            return message.getClass().getSimpleName();
        }

        private boolean readyState() {
            // skip pending queue state : DelayedStream
            if (this.onReadyState.get() == false) {
                final long pendingCount = this.pendingCounter.incrementAndGet();
                if (pendingCount > this.maxPendingThreshold) {
                    return false;
                }
                return true;
            }
            return isReady();
        }

        @Override
        public void cancel(String message, Throwable cause) {
            logger.info("Cancel message. message={}, cause={}", message, cause.getMessage(), cause);
            super.cancel(message, cause);
        }

        @VisibleForTesting
        boolean getOnReadyState() {
            return onReadyState.get();
        }


        @VisibleForTesting
        long getDiscardCounter() {
            return discardCounter.get();
        }

        @VisibleForTesting
        public long getPendingCount() {
            return pendingCounter.get();
        }
    }
}
