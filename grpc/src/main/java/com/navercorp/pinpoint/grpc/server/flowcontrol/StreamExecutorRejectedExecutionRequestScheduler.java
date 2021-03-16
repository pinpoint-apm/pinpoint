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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author jaehong.kim
 */
public class StreamExecutorRejectedExecutionRequestScheduler {
//    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ScheduledExecutorService scheduledExecutorService;
    private final RejectedExecutionListenerFactory rejectedExecutionListenerFactory;

    private final int periodMillis;


    public StreamExecutorRejectedExecutionRequestScheduler(final ScheduledExecutorService scheduledExecutorService, final int periodMillis,
                                                           final RejectedExecutionListenerFactory rejectedExecutionListenerFactory) {

        this.scheduledExecutorService = Objects.requireNonNull(scheduledExecutorService, "scheduledExecutorService");
        this.periodMillis = periodMillis;

        this.rejectedExecutionListenerFactory = Objects.requireNonNull(rejectedExecutionListenerFactory, "rejectedExecutionListenerFactory");

    }

    public Listener schedule(final ServerCallWrapper serverCall) {
        final RejectedExecutionListener rejectedExecutionListener = rejectedExecutionListenerFactory.newListener(serverCall);
        final RequestScheduleJob command = new RequestScheduleJob(rejectedExecutionListener);
        final ScheduledFuture<?> future = scheduledExecutorService.scheduleAtFixedRate(command, periodMillis, periodMillis, TimeUnit.MILLISECONDS);
        command.setFuture(future);
        final Listener listener = new Listener(rejectedExecutionListener, future);
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

    @VisibleForTesting
    static class RequestScheduleJob implements Runnable {
        private final RejectedExecutionListener listener;
        private volatile ScheduledFuture<?> future;

        public RequestScheduleJob(final RejectedExecutionListener listener) {
            this.listener = Objects.requireNonNull(listener, "listener");
        }

        @Override
        public void run() {
            if (!expireIdleTimeout()) {
                listener.onSchedule();
            }
        }

        private boolean expireIdleTimeout() {
            if (listener.idleTimeExpired()) {
                if (cancel(this.future)) {
                    listener.idleTimeout();
                    return true;
                }
            }
            return false;
        }

        private boolean cancel(ScheduledFuture<?> future) {
            if (future == null) {
                return false;
            }
            return future.cancel(false);
        }

        public void setFuture(ScheduledFuture<?> future) {
            this.future = Objects.requireNonNull(future, "future");
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

        public void onMessage() {
            this.rejectedExecutionListener.onMessage();
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