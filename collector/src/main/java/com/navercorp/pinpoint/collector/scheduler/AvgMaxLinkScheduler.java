/*
 * Copyright 2023 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.collector.scheduler;

import com.navercorp.pinpoint.collector.dao.CachedStatisticsDao;
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;


@Component
public class AvgMaxLinkScheduler {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final TaskScheduler scheduler;
    private final List<CachedStatisticsDao> statisticsDaoList;
    private final Duration flushInterval;
    private final boolean jitterEnabled;
    private final double jitterSpread;


    public AvgMaxLinkScheduler(@Qualifier("statisticsLinkScheduler") TaskScheduler scheduler,
                               @Value("${collector.map-link.avg.flush-period:5000}") Duration flushInterval,
                               @Value("${collector.map-link.avg.jitter.enabled:false}") boolean jitterEnabled,
                               @Value("${collector.map-link.avg.jitter.spread:0.5}") double jitterSpread,
                               List<CachedStatisticsDao> statisticsDaoList) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.statisticsDaoList = Objects.requireNonNull(statisticsDaoList, "statisticsDaoList");
        this.flushInterval = Objects.requireNonNull(flushInterval, "flushInterval");
        this.jitterEnabled = jitterEnabled;
        this.jitterSpread = jitterSpread;
        logger.info("AvgMaxLinkScheduler flushPeriod={}, jitterEnabled={}, jitterSpread={}", 
                    flushInterval, jitterEnabled, jitterSpread);
        logger.info("AvgMaxLinkScheduler CachedStatisticsDao:{}", statisticsDaoList);
    }

    @PostConstruct
    public void linkScheduling()  {
        for (CachedStatisticsDao dao : statisticsDaoList) {
            if (jitterEnabled) {
                scheduleWithJitter(dao);
            } else {
                this.scheduler.scheduleWithFixedDelay(dao::flushAvgMax, flushInterval);
            }
        }
    }

    private void scheduleWithJitter(CachedStatisticsDao dao) {
        Jitter jitter = new Jitter(jitterSpread);
        
        // Create a task that reschedules itself with a new random delay each time
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    dao.flushAvgMax();
                } finally {
                    // Calculate new jitter for next execution
                    long jitteredDelay = jitter.nextDelay(flushInterval.toMillis());
                    Duration delayWithJitter = Duration.ofMillis(jitteredDelay);
                    
                    logger.debug("AvgMaxLink rescheduling {}: base interval={}ms, jittered delay={}ms", 
                                dao.getClass().getSimpleName(), flushInterval.toMillis(), jitteredDelay);
                    
                    // Reschedule with new jitter
                    scheduler.schedule(this, Instant.now().plus(delayWithJitter));
                }
            }
        };
        
        // Initial scheduling with jitter
        long initialJitteredDelay = jitter.nextDelay(flushInterval.toMillis());
        Duration initialDelay = Duration.ofMillis(initialJitteredDelay);
        
        logger.info("AvgMaxLink scheduler started for {}: base interval={}ms, initial jittered delay={}ms", 
                    dao.getClass().getSimpleName(), flushInterval.toMillis(), initialJitteredDelay);
        
        scheduler.schedule(task, Instant.now().plus(initialDelay));
    }
}
