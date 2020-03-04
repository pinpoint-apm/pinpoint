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

package com.navercorp.pinpoint.grpc.server;

import io.grpc.internal.NoopServerCall;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StreamExecutorRejectedExecutionRequestSchedulerTest {

    private ScheduledExecutorService scheduledExecutorService;

    @Before
    public void setUp() throws Exception {
        this.scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    }

    @After
    public void tearDown() throws Exception {
        if (this.scheduledExecutorService != null) {
            this.scheduledExecutorService.shutdownNow();
        }
    }

    @Test
    public void schedule() {

        StreamExecutorRejectedExecutionRequestScheduler scheduler = new StreamExecutorRejectedExecutionRequestScheduler(scheduledExecutorService, 1000, 10);
        StreamExecutorRejectedExecutionRequestScheduler.Listener listener = scheduler.schedule(new NoopServerCall());
        assertEquals(0, listener.getRejectedExecutionCount());
        listener.onRejectedExecution();
        assertEquals(1, listener.getRejectedExecutionCount());

        ScheduledFuture scheduledFuture = listener.getRequestScheduledFuture();
        assertFalse(scheduledFuture.isCancelled());
        listener.onCancel();
        assertTrue(scheduledFuture.isCancelled());
    }
}