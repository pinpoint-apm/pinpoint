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

import java.util.Objects;
import java.util.concurrent.Future;

/**
 * @author jaehong.kim
 */
public class StreamExecutorRejectedExecutionRequestScheduler {
//    private final Logger logger = LogManager.getLogger(this.getClass());

    private final ScheduledExecutor scheduledExecutor;
    private final RejectedExecutionListenerFactory rejectedExecutionListenerFactory;

    public StreamExecutorRejectedExecutionRequestScheduler(final ScheduledExecutor scheduledExecutor,
                                                           final RejectedExecutionListenerFactory rejectedExecutionListenerFactory) {
        this.scheduledExecutor = Objects.requireNonNull(scheduledExecutor, "scheduledExecutor");

        this.rejectedExecutionListenerFactory = Objects.requireNonNull(rejectedExecutionListenerFactory, "rejectedExecutionListenerFactory");

    }

    public Listener schedule(final ServerCallWrapper serverCall) {
        final RejectedExecutionListener rejectedExecutionListener = rejectedExecutionListenerFactory.newListener(serverCall);
        final Runnable command = new RequestScheduleJob(rejectedExecutionListener);
        final Future<?> future = scheduledExecutor.schedule(command);

        rejectedExecutionListener.setFuture(future);

        return new Listener(rejectedExecutionListener);
    }

    public static class RequestScheduleJob implements Runnable {
        private final RejectedExecutionListener rejectedExecutionListener;

        public RequestScheduleJob(final RejectedExecutionListener rejectedExecutionListener) {
            this.rejectedExecutionListener = Objects.requireNonNull(rejectedExecutionListener, "rejectedExecutionListener");
        }

        @Override
        public void run() {
            rejectedExecutionListener.onSchedule();
        }

        public RejectedExecutionListener getRejectedExecutionListener() {
            return rejectedExecutionListener;
        }
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("StreamExecutorRejectedExecutionRequestScheduler{");
        sb.append("scheduledExecutorService=").append(scheduledExecutor);
        sb.append(", rejectedExecutionListenerFactory=").append(rejectedExecutionListenerFactory);
        sb.append('}');
        return sb.toString();
    }

    public static class Listener {
        private final RejectedExecutionListener rejectedExecutionListener;

        public Listener(RejectedExecutionListener rejectedExecutionListener) {
            this.rejectedExecutionListener = Objects.requireNonNull(rejectedExecutionListener, "rejectedExecutionListener");
        }

        public void onRejectedExecution() {
            this.rejectedExecutionListener.onRejectedExecution();
        }

        public void onCancel() {
            this.rejectedExecutionListener.cancel();
        }

        public long getRejectedExecutionCount() {
            return this.rejectedExecutionListener.getRejectedExecutionCount();
        }

        public boolean isCancelled() {
            return rejectedExecutionListener.isCancelled();
        }

        public void onMessage() {
            this.rejectedExecutionListener.onMessage();
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Listener{");
            sb.append("rejectedExecutionListener=").append(rejectedExecutionListener);
            sb.append('}');
            return sb.toString();
        }
    }

}