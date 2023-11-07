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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.Objects;


@Component
public class StatisticsLinkScheduler {

    private final TaskScheduler scheduler;
    private final List<CachedStatisticsDao> statisticsDaoList;

    public StatisticsLinkScheduler(@Qualifier("avgMaxLinkScheduler") TaskScheduler scheduler,
                                   List<CachedStatisticsDao> statisticsDaoList) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
        this.statisticsDaoList = Objects.requireNonNull(statisticsDaoList, "statisticsDaoList");
    }

    @PostConstruct
    public void linkScheduling()  {
        for (CachedStatisticsDao dao : statisticsDaoList) {
            this.scheduler.scheduleWithFixedDelay(dao::flushLink, Duration.ofMillis(1000));
        }
    }
}
