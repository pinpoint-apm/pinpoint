/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.collector.heatmap.config;

import com.navercorp.pinpoint.collector.heatmap.counter.HeatmapCounter;
import com.navercorp.pinpoint.collector.heatmap.dao.HeatmapDao;
import com.navercorp.pinpoint.collector.heatmap.scheduler.HeatmapFlusher;
import com.navercorp.pinpoint.collector.heatmap.vo.HeatmapStatKey;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@ConditionalOnExpression("${kafka.heatmap.aggregation.interval-ms:0} > 0")
public class HeatmapSchedulerConfiguration {

    @Bean
    public HeatmapCounter<HeatmapStatKey> heatmapStatCounter() {
        return new HeatmapCounter<>();
    }

    @Bean
    public TaskScheduler heatmapStatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("Pinpoint-HeatmapStat-Flusher-");
        scheduler.setDaemon(true);
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(10);
        return scheduler;
    }

    @Bean
    public HeatmapFlusher<HeatmapStatKey> heatmapStatFlusher(HeatmapCounter<HeatmapStatKey> heatmapStatCounter,
                                                             HeatmapDao heatmapDao,
                                                             @Qualifier("heatmapStatScheduler") TaskScheduler scheduler,
                                                             HeatmapProperties heatmapProperties) {
        return new HeatmapFlusher<>("app", heatmapStatCounter,
                heatmapDao::insert,
                scheduler, heatmapProperties.getAggregationInterval());
    }
}
