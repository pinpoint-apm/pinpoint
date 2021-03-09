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

package com.navercorp.pinpoint.grpc.server.flowcontrol;

import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import io.grpc.ServerCall;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author jaehong.kim
 */
public class StreamExecutorRejectedExecutionRequestScheduler {
    private static final int REQUEST_IMMEDIATELY = -1;
    private final int periodMillis;
    private final ScheduledExecutorService scheduledExecutorService;
    private final long recoveryMessagesCount;

    public StreamExecutorRejectedExecutionRequestScheduler(final ScheduledExecutorService scheduledExecutorService, final int periodMillis, int recoveryMessagesCount) {
        this.scheduledExecutorService = Objects.requireNonNull(scheduledExecutorService, "scheduledExecutorService");
        this.periodMillis = periodMillis;
        // cast long
        this.recoveryMessagesCount = recoveryMessagesCount;
    }

    public <ReqT, RespT> Listener schedule(final ServerCall<ReqT, RespT> call) {
        final ServerCallWrapper serverCall = new DefaultServerCallWrapper<>(call);

        final RejectedExecutionListener rejectedExecutionListener = newRejectedExecutionListener(serverCall);
        final RequestScheduleJob command = new RequestScheduleJob(rejectedExecutionListener);
        final ScheduledFuture<?> requestScheduledFuture = scheduledExecutorService.scheduleAtFixedRate(command, periodMillis, periodMillis, TimeUnit.MILLISECONDS);
        final Listener listener = new Listener(rejectedExecutionListener, requestScheduledFuture);
        return listener;
    }

    private RejectedExecutionListener newRejectedExecutionListener(ServerCallWrapper serverCall) {
        if (recoveryMessagesCount == REQUEST_IMMEDIATELY) {
            return new SimpleRejectedExecutionListener(serverCall);
        } else {
            return new ControlFlowRejectExecutionListener(serverCall, recoveryMessagesCount);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StreamExecutorRejectedExecutionRequestScheduler{");
        sb.append("scheduledExecutorService=").append(scheduledExecutorService);
        sb.append(", periodMillis=").append(periodMillis);
        sb.append('}');
        return sb.toString();
    }

    @VisibleForTesting
    static class RequestScheduleJob implements Runnable {
        private final RejectedExecutionListener listener;

        public RequestScheduleJob(final RejectedExecutionListener listener) {
            this.listener = Objects.requireNonNull(listener, "listener");
        }

        @Override
        public void run() {
            listener.onSchedule();
        }
    }

    public static class Listener {
        private final RejectedExecutionListener rejectedExecutionListener;
        private final ScheduledFuture<?> requestScheduledFuture;

        public Listener(RejectedExecutionListener rejectedExecutionListener, ScheduledFuture<?> requestScheduledFuture) {
            this.rejectedExecutionListener = Objects.requireNonNull(rejectedExecutionListener, "rejectedExecutionListener");
            this.requestScheduledFuture = Objects.requireNonNull(requestScheduledFuture, "requestScheduledFuture");
        }

        public void onRejectedExecution() {
            this.rejectedExecutionListener.onRejectedExecution();
        }

        public void onCancel() {
            this.requestScheduledFuture.cancel(false);
        }

        public long getRejectedExecutionCount() {
            return this.rejectedExecutionListener.getRejectedExecutionCount();
        }

        public boolean isCancelled() {
            return this.requestScheduledFuture.isCancelled();
        }

        public void onExecute() {
            this.rejectedExecutionListener.onExecute();
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

    public interface ServerCallWrapper {
        void request(int numMessages);
    }

    private static class DefaultServerCallWrapper<ReqT, RespT> implements ServerCallWrapper {
        private final ServerCall<ReqT, RespT> serverCall;

        public DefaultServerCallWrapper(ServerCall<ReqT, RespT> serverCall) {
            this.serverCall = Objects.requireNonNull(serverCall, "serverCall");
        }

        @Override
        public void request(int numMessages) {
            serverCall.request(numMessages);
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("DefaultServerCallWrapper{");
            sb.append("serverCall=").append(serverCall);
            sb.append('}');
            return sb.toString();
        }
    }
}