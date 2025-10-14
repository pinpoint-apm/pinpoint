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
import jakarta.annotation.PostConstruct;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;


@Component
public class StatisticsLinkScheduler {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final TaskScheduler scheduler;
    private final List<CachedStatisticsDao> statisticsDaoList;
    private final StartTimeDistributor startTimeDistributor;
    private final Duration flushInterval;

    public StatisticsLinkScheduler(@Qualifier("avgMaxLinkScheduler") TaskScheduler scheduler,
                                   @Value("${collector.map-link.stat.flush-interval:5000}") Duration flushInterval,
                                   StartTimeDistributor startTimeDistributor,
                                   List<CachedStatisticsDao> statisticsDaoList) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.statisticsDaoList = Objects.requireNonNull(statisticsDaoList, "statisticsDaoList");
        this.startTimeDistributor = Objects.requireNonNull(startTimeDistributor, "startTimeDistributor");
        this.flushInterval = Objects.requireNonNull(flushInterval, "flushInterval");
        logger.info("StatisticsLinkScheduler flushInterval={}", flushInterval);
        logger.info("StatisticsLinkScheduler startTimeDistributor:{}", startTimeDistributor);
        logger.info("StatisticsLinkScheduler CachedStatisticsDao:{}", statisticsDaoList);
    }

    @PostConstruct
    public void linkScheduling()  {
        final long intervalMillis = flushInterval.toMillis();
        final long now = System.currentTimeMillis();

        for (CachedStatisticsDao dao : statisticsDaoList) {
            long nextTick = startTimeDistributor.nextTick();
            Instant startInstant = Instant.ofEpochMilli(now + intervalMillis + nextTick);
            logger.info("{} started for {}: interval={}ms, nextTick={}ms, startTime={}",
                    ClassUtils.getShortName(this.getClass()), ClassUtils.getShortName(dao.getClass()), intervalMillis, nextTick, startInstant);
            this.scheduler.scheduleWithFixedDelay(dao::flushLink, startInstant, flushInterval);
        }
    }

}
