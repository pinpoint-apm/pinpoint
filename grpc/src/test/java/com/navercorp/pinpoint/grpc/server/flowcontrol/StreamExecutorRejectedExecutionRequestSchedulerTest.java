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

import io.grpc.Metadata;
import io.grpc.Status;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class StreamExecutorRejectedExecutionRequestSchedulerTest {

    @Test
    public void schedule() {
        ScheduledExecutor scheduledExecutor = new ScheduledExecutor() {
            @Override
            public Future<?> schedule(Runnable command) {
                FutureTask<?> futureTask = new FutureTask<>(command, null);
//                futureTask.run();
                return futureTask;
            }
        };

        IdleTimeoutFactory idleTimeoutFactory = new IdleTimeoutFactory(5000);
        RejectedExecutionListenerFactory listenerFactory = new RejectedExecutionListenerFactory("test", 10, idleTimeoutFactory);
        StreamExecutorRejectedExecutionRequestScheduler scheduler = new StreamExecutorRejectedExecutionRequestScheduler(scheduledExecutor, listenerFactory);
        ServerCallWrapper serverCallWrapper = mock(ServerCallWrapper.class);

        StreamExecutorRejectedExecutionRequestScheduler.Listener listener = scheduler.schedule(serverCallWrapper);

        assertFalse(listener.isCancelled());
        listener.onCancel();
        assertTrue(listener.isCancelled());
    }

    @Test
    public void schedule_timeout() {
        final AtomicReference<Runnable> reference = new AtomicReference<>();

        ScheduledExecutor scheduledExecutor = new ScheduledExecutor() {
            @Override
            public Future<?> schedule(Runnable command) {
                reference.set(command);

                FutureTask<?> futureTask = new FutureTask<>(command, null);
                return futureTask;
            }
        };

        IdleTimeoutFactory idleTimeoutFactory = new IdleTimeoutFactory(0);
        RejectedExecutionListenerFactory listenerFactory = new RejectedExecutionListenerFactory("test", 10, idleTimeoutFactory);
        StreamExecutorRejectedExecutionRequestScheduler scheduler = new StreamExecutorRejectedExecutionRequestScheduler(scheduledExecutor, listenerFactory);
        ServerCallWrapper serverCallWrapper = mock(ServerCallWrapper.class);

        StreamExecutorRejectedExecutionRequestScheduler.Listener listener = scheduler.schedule(serverCallWrapper);

        //  emulation async run
        Runnable runnable = reference.get();
        runnable.run();


        verify(serverCallWrapper).cancel(ArgumentMatchers.any(Status.class), ArgumentMatchers.any(Metadata.class));
        assertTrue(listener.isCancelled());
    }

    @Test
    public void schedule_noTimeout() {
        final AtomicReference<Runnable> capture = new AtomicReference<>();

        ScheduledExecutor scheduledExecutor = new ScheduledExecutor() {
            @Override
            public Future<?> schedule(Runnable command) {
                capture.set(command);

                FutureTask<?> futureTask = new FutureTask<>(command, null);
                return futureTask;
            }
        };

        IdleTimeoutFactory noTimeout = new IdleTimeoutFactory(TimeUnit.SECONDS.toMillis(600));

        RejectedExecutionListenerFactory listenerFactory = new RejectedExecutionListenerFactory("test", 10, noTimeout);
        StreamExecutorRejectedExecutionRequestScheduler scheduler = new StreamExecutorRejectedExecutionRequestScheduler(scheduledExecutor, listenerFactory);
        ServerCallWrapper serverCallWrapper = mock(ServerCallWrapper.class);

        StreamExecutorRejectedExecutionRequestScheduler.Listener listener = scheduler.schedule(serverCallWrapper);

        //  emulation async run
        Runnable runnable = capture.get();
        runnable.run();

        verify(serverCallWrapper, never()).cancel(ArgumentMatchers.any(Status.class), ArgumentMatchers.any(Metadata.class));
        assertFalse(listener.isCancelled());
    }
}