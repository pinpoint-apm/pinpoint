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

package com.navercorp.pinpoint.collector.config;

import com.navercorp.pinpoint.collector.dao.CachedStatisticsDao;
import com.navercorp.pinpoint.collector.scheduler.AvgMaxLinkScheduler;
import com.navercorp.pinpoint.collector.scheduler.StatisticsLinkScheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.util.List;

@Configuration
@Import({
        AvgMaxLinkScheduler.class,
        StatisticsLinkScheduler.class
})
public class SchedulerConfiguration {

    @Bean
    public TaskScheduler statisticsLinkScheduler(List<CachedStatisticsDao> statisticsDaoList) {
        int coreSize = getCoreSize(statisticsDaoList);
        return newThreadPoolTaskScheduler(coreSize, "Pinpoint-AutoFlusher-LINK-");
    }

    @Bean
    public TaskScheduler avgMaxLinkScheduler(List<CachedStatisticsDao> statisticsDaoList) {
        int coreSize = getCoreSize(statisticsDaoList);
        return newThreadPoolTaskScheduler(coreSize, "Pinpoint-AutoFlusher-AVG/MAX-");
    }

    private int getCoreSize(List<CachedStatisticsDao> statisticsDaoList) {
        return Math.max(1, statisticsDaoList.size());
    }

    private ThreadPoolTaskScheduler newThreadPoolTaskScheduler(int coreSize, String threadNamePrefix) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(coreSize);
        scheduler.setThreadNamePrefix(threadNamePrefix);
        scheduler.setDaemon(true);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(10);
        return scheduler;
    }
}
