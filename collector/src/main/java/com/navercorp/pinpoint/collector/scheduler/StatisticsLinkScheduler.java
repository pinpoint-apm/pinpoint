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
import java.util.concurrent.ThreadLocalRandom;


@Component
public class StatisticsLinkScheduler {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final TaskScheduler scheduler;
    private final List<CachedStatisticsDao> statisticsDaoList;
    private final Duration flushInterval;

    public StatisticsLinkScheduler(@Qualifier("avgMaxLinkScheduler") TaskScheduler scheduler,
                                   @Value("${collector.map-link.stat.flush-interval:5000}") Duration flushInterval,
                                   List<CachedStatisticsDao> statisticsDaoList) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.statisticsDaoList = Objects.requireNonNull(statisticsDaoList, "statisticsDaoList");
        this.flushInterval = Objects.requireNonNull(flushInterval, "flushInterval");
        logger.info("StatisticsLinkScheduler flushInterval={}", flushInterval);
        logger.info("StatisticsLinkScheduler CachedStatisticsDao:{}", statisticsDaoList);
    }

    @PostConstruct
    public void linkScheduling()  {
        final long intervalMillis = flushInterval.toMillis();

        int numDaos = statisticsDaoList.size();
        long jitterUnit = intervalMillis / numDaos;
        final long[] jitterArray = new long[numDaos];
        if (jitterUnit > 0) {
            for (int i = 0; i < numDaos; i++) {
                jitterArray[i] = ThreadLocalRandom.current().nextLong(i * jitterUnit, (i + 1) * jitterUnit);
            }
        } else {
            for (int i = 0; i < numDaos; i++) {
                jitterArray[i] = 0L;
            }
        }

        final Instant now = Instant.now();
        for (int i = 0; i < numDaos; i++) {
            CachedStatisticsDao dao = statisticsDaoList.get(i);
            Instant startInstant = now.plusMillis(intervalMillis + jitterArray[i]);
            logger.info("{} started for {}: interval={}ms, jitter={}ms, startTime={}",
                    this.getClass().getSimpleName(), dao.getClass().getSimpleName(), intervalMillis, jitterArray[i], startInstant);
            this.scheduler.scheduleAtFixedRate(dao::flushLink, startInstant, flushInterval);
        }
    }
}
