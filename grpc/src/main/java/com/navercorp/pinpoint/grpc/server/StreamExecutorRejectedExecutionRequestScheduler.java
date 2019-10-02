/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.grpc.server;

import com.google.common.annotations.VisibleForTesting;
import com.navercorp.pinpoint.common.util.Assert;
import io.grpc.ServerCall;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author jaehong.kim
 */
public class StreamExecutorRejectedExecutionRequestScheduler {
    private final int periodMillis;
    private final ScheduledExecutorService scheduledExecutorService;

    public StreamExecutorRejectedExecutionRequestScheduler(final ScheduledExecutorService scheduledExecutorService, final int periodMillis) {
        this.scheduledExecutorService = scheduledExecutorService;
        this.periodMillis = periodMillis;
    }

    public <ReqT, RespT> Listener schedule(final ServerCall<ReqT, RespT> call) {
        final ServerCallWrapper serverCall = new ServerCallWrapper() {
            @Override
            public void request(int numMessages) {
                call.request(numMessages);
            }
        };
        final RejectedExecutionListener rejectedExecutionListener = new RejectedExecutionListener(serverCall);
        final ScheduledFuture requestScheduledFuture = scheduledExecutorService.scheduleAtFixedRate(new RequestScheduleJob(rejectedExecutionListener), periodMillis, periodMillis, TimeUnit.MILLISECONDS);
        final Listener listener = new Listener(rejectedExecutionListener, requestScheduledFuture);
        return listener;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StreamExecutorRejectedExecutionRequestScheduler{");
        sb.append("scheduledExecutorService=").append(scheduledExecutorService);
        sb.append(", periodMillis=").append(periodMillis);
        sb.append('}');
        return sb.toString();
    }

    private interface ServerCallWrapper {
        void request(int numMessages);
    }

    @VisibleForTesting
    static class RejectedExecutionListener {
        private final AtomicInteger rejectedExecutionCounter = new AtomicInteger(0);
        private final ServerCallWrapper serverCall;

        public RejectedExecutionListener(ServerCallWrapper serverCall) {
            this.serverCall = serverCall;
        }

        public void onRejectedExecution() {
            this.rejectedExecutionCounter.incrementAndGet();
        }

        public void onSchedule() {
            if (this.rejectedExecutionCounter.get() > 0) {
                serverCall.request(this.rejectedExecutionCounter.getAndSet(0));
            }
        }

        public int getRejectedExecutionCount() {
            return rejectedExecutionCounter.get();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("RejectedExecutionListener{");
            sb.append("rejectedExecutionCounter=").append(rejectedExecutionCounter);
            sb.append(", serverCall=").append(serverCall);
            sb.append('}');
            return sb.toString();
        }
    }

    @VisibleForTesting
    static class RequestScheduleJob implements Runnable {
        private final RejectedExecutionListener listener;

        public RequestScheduleJob(final RejectedExecutionListener listener) {
            this.listener = listener;
        }

        @Override
        public void run() {
            listener.onSchedule();
        }
    }

    public static class Listener {
        private final RejectedExecutionListener rejectedExecutionListener;
        private final ScheduledFuture requestScheduledFuture;

        public Listener(RejectedExecutionListener rejectedExecutionListener, ScheduledFuture requestScheduledFuture) {
            this.rejectedExecutionListener = Assert.requireNonNull(rejectedExecutionListener, "rejectedExecutionListener");
            this.requestScheduledFuture = Assert.requireNonNull(requestScheduledFuture, "requestScheduledFuture");
        }

        public void onRejectedExecution() {
            this.rejectedExecutionListener.onRejectedExecution();
        }

        public void onCancel() {
            this.requestScheduledFuture.cancel(false);
        }

        public int getRejectedExecutionCount() {
            return this.rejectedExecutionListener.getRejectedExecutionCount();
        }

        @VisibleForTesting
        ScheduledFuture getRequestScheduledFuture() {
            return requestScheduledFuture;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Listener{");
            sb.append("rejectedExecutionListener=").append(rejectedExecutionListener);
            sb.append(", requestScheduledFuture=").append(requestScheduledFuture);
            sb.append('}');
            return sb.toString();
        }
    }
}