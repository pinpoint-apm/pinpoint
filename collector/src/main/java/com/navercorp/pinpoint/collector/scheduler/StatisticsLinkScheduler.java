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
import java.util.Random;


@Component
public class StatisticsLinkScheduler {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final Random random = new Random();

    private final TaskScheduler scheduler;
    private final List<CachedStatisticsDao> statisticsDaoList;
    private final Duration flushInterval;
    private final boolean jitterEnabled;
    private final long maxJitterMillis;

    public StatisticsLinkScheduler(@Qualifier("avgMaxLinkScheduler") TaskScheduler scheduler,
                                   @Value("${collector.map-link.stat.flush-interval:5000}") Duration flushInterval,
                                   @Value("${collector.map-link.stat.jitter.enabled:false}") boolean jitterEnabled,
                                   @Value("${collector.map-link.stat.jitter.max-millis:#{null}}") Long maxJitterMillis,
                                   List<CachedStatisticsDao> statisticsDaoList) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.statisticsDaoList = Objects.requireNonNull(statisticsDaoList, "statisticsDaoList");
        this.flushInterval = Objects.requireNonNull(flushInterval, "flushInterval");
        this.jitterEnabled = jitterEnabled;
        // Default max jitter to the flush interval if not specified
        this.maxJitterMillis = (maxJitterMillis != null) ? maxJitterMillis : flushInterval.toMillis();
        logger.info("StatisticsLinkScheduler flushInterval={}, jitterEnabled={}, maxJitterMillis={}", 
                    flushInterval, jitterEnabled, this.maxJitterMillis);
        logger.info("StatisticsLinkScheduler CachedStatisticsDao:{}", statisticsDaoList);
    }

    @PostConstruct
    public void linkScheduling()  {
        for (CachedStatisticsDao dao : statisticsDaoList) {
            if (jitterEnabled) {
                scheduleWithJitter(dao);
            } else {
                this.scheduler.scheduleWithFixedDelay(dao::flushLink, flushInterval);
            }
        }
    }

    private void scheduleWithJitter(CachedStatisticsDao dao) {
        // Create a task that reschedules itself with a new random delay each time
        Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    dao.flushLink();
                } finally {
                    // Calculate new jitter for next execution
                    long jitterMillis = random.nextLong(maxJitterMillis);
                    Duration delayWithJitter = flushInterval.plus(Duration.ofMillis(jitterMillis));
                    
                    logger.debug("StatisticsLink rescheduling {}: interval={}ms, jitter={}ms", 
                                dao.getClass().getSimpleName(), flushInterval.toMillis(), jitterMillis);
                    
                    // Reschedule with new jitter
                    scheduler.schedule(this, Instant.now().plus(delayWithJitter));
                }
            }
        };
        
        // Initial scheduling with jitter
        long initialJitterMillis = random.nextLong(maxJitterMillis);
        Duration initialDelay = Duration.ofMillis(initialJitterMillis);
        
        logger.info("StatisticsLink scheduler started for {}: interval={}ms, initial jitter={}ms", 
                    dao.getClass().getSimpleName(), flushInterval.toMillis(), initialJitterMillis);
        
        scheduler.schedule(task, Instant.now().plus(initialDelay));
    }
}
