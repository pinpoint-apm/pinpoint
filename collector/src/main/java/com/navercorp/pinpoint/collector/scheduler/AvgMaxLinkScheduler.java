/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.scheduler;

import com.navercorp.pinpoint.collector.dao.CachedStatisticsDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ScheduledFuture;


/**
 * Flushes the cached statistics periodically, started and stopped by the context as a {@link SmartLifecycle}.
 * <p>
 * {@link #stop()} flushes what is left in the caches once more. The {@link #PHASE} is below the default
 * phase of the receivers, so the last flush runs after the receivers have stopped and before the
 * HBase connection is closed with the other beans. The last flush is asynchronous and is not awaited.
 */
@Component
public class AvgMaxLinkScheduler implements SmartLifecycle {

    public static final int PHASE = SmartLifecycle.DEFAULT_PHASE - 1;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final TaskScheduler scheduler;
    private final List<CachedStatisticsDao> statisticsDaoList;
    private final StartTimeDistributor startTimeDistributor;
    private final Duration flushInterval;

    private final List<ScheduledFuture<?>> flushTasks = new ArrayList<>();
    private volatile boolean running;

    public AvgMaxLinkScheduler(@Qualifier("avgMaxLinkScheduler") TaskScheduler scheduler,
                               @Value("${collector.map-link.avg.flush-period:5000}") Duration flushInterval,
                               StartTimeDistributor startTimeDistributor,
                               List<CachedStatisticsDao> statisticsDaoList) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.statisticsDaoList = Objects.requireNonNull(statisticsDaoList, "statisticsDaoList");
        this.startTimeDistributor = Objects.requireNonNull(startTimeDistributor, "startTimeDistributor");
        this.flushInterval = Objects.requireNonNull(flushInterval, "flushInterval");
        logger.info("AvgMaxLinkScheduler flushPeriod={}", flushInterval);
        logger.info("AvgMaxLinkScheduler startTimeDistributor:{}", startTimeDistributor);
        logger.info("AvgMaxLinkScheduler CachedStatisticsDao:{}", statisticsDaoList);
    }

    @Override
    public void start() {
        if (this.running) {
            return;
        }
        final long intervalMillis = flushInterval.toMillis();
        final long now = System.currentTimeMillis();

        for (CachedStatisticsDao dao : statisticsDaoList) {
            long nextTick = startTimeDistributor.nextTick();
            Instant startInstant = Instant.ofEpochMilli(now + intervalMillis + nextTick);
            logger.info("{} started for {}: interval={}ms, nextTick={}ms, startTime={}",
                    ClassUtils.getShortName(this.getClass()), ClassUtils.getShortName(dao.getClass()), intervalMillis, nextTick, startInstant);
            this.flushTasks.add(this.scheduler.scheduleWithFixedDelay(dao::flushAvgMax, startInstant, flushInterval));
        }
        this.running = true;
    }

    @Override
    public void stop() {
        logger.info("{} stopped", ClassUtils.getShortName(this.getClass()));
        for (ScheduledFuture<?> flushTask : this.flushTasks) {
            if (flushTask != null) {
                flushTask.cancel(false);
            }
        }
        this.flushTasks.clear();
        // the last flush: the receivers have stopped, the HBase connection is still open
        for (CachedStatisticsDao dao : statisticsDaoList) {
            try {
                dao.flushAvgMax();
            } catch (Exception e) {
                logger.warn("{} last flush failed. {}", ClassUtils.getShortName(this.getClass()), ClassUtils.getShortName(dao.getClass()), e);
            }
        }
        this.running = false;
    }

    @Override
    public boolean isRunning() {
        return this.running;
    }

    @Override
    public int getPhase() {
        return PHASE;
    }
}
