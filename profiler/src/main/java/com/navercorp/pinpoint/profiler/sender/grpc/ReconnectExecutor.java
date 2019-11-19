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

import com.navercorp.pinpoint.common.util.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;


/**
 * @author Woonduk Kang(emeroad)
 */
public class ReconnectExecutor {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private volatile boolean shutdown;
    private final ScheduledExecutorService scheduledExecutorService;
    private final AtomicLong rejectedCounter = new AtomicLong();

    public ReconnectExecutor(ScheduledExecutorService scheduledExecutorService) {
        this.scheduledExecutorService = Assert.requireNonNull(scheduledExecutorService, "scheduledExecutorService");
    }

    private void execute0(Runnable command) {
        Assert.requireNonNull(command, "command");

        if (shutdown) {
            logger.debug("already shutdown");
            return;
        }
        if (command instanceof ReconnectJob) {
            ReconnectJob reconnectJob = (ReconnectJob) command;
            try {
                scheduledExecutorService.schedule(reconnectJob, reconnectJob.nextBackoffNanos(), TimeUnit.NANOSECONDS);
            } catch (RejectedExecutionException e) {
                final long failCount = rejectedCounter.incrementAndGet();
                logger.info("{} reconnectJob scheduled fail {}", command, failCount);
            }
        } else {
            throw new IllegalArgumentException("unknown command type " + command);
        }
    }

    public void close() {
        shutdown = true;
    }

    public Reconnector newReconnector(Runnable reconnectJob) {
        Assert.requireNonNull(reconnectJob, "reconnectJob");
        if (logger.isInfoEnabled()) {
            logger.info("newReconnector(reconnectJob = [{}])", reconnectJob);
        }

        final Executor dispatch = new Executor() {
            @Override
            public void execute(Runnable command) {
                ReconnectExecutor.this.execute0(command);
            }
        };
        final ReconnectJob reconnectJobWrap = wrapReconnectJob(reconnectJob);
        return new ReconnectAdaptor(dispatch, reconnectJobWrap);
    }


    private ReconnectJob wrapReconnectJob(Runnable runnable) {
        return new ExponentialBackoffReconnectJob(runnable);
    }
}
