/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.monitor;

import com.navercorp.pinpoint.common.util.PinpointThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Taejin Koo
 */
public class DefaultDeadlockMonitor implements DeadlockMonitor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ScheduledExecutorService executor = new ScheduledThreadPoolExecutor(1, new PinpointThreadFactory("Pinpoint-deadlock-monitor", true));

    private final boolean enable;
    private final long intervalMillis;
    private final DeadlockMonitorJob deadlockMonitorJob;

    public DefaultDeadlockMonitor(boolean enable, long intervalMillis, DeadlockThreadRegistry deadlockThreadRegistry) {
        this.enable = enable;
        this.intervalMillis = intervalMillis;
        this.deadlockMonitorJob = new DeadlockMonitorJob(deadlockThreadRegistry);

        startForPreload();
    }

    private void startForPreload() {
        if (enable) {
            deadlockMonitorJob.run();
        }
    }

    @Override
    public void start() {
        if (enable) {
            executor.scheduleAtFixedRate(deadlockMonitorJob, intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
            logger.info("DefaultDeadlockMonitor started");
        } else {
            logger.info("DefaultDeadlockMonitor not started. caused profiler.monitor.deadlock.enable=false.");
        }
    }

    @Override
    public void stop() {
        if (enable) {
            executor.shutdown();
            try {
                this.executor.awaitTermination(3000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            logger.info("DefaultDeadlockMonitor stopped");
        }
    }

}
