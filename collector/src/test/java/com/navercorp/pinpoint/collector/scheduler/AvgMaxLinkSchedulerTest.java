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
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author intr3p1d
 */
public class AvgMaxLinkSchedulerTest {

    @Test
    public void testSchedulerWithoutJitter() {
        TaskScheduler taskScheduler = createTaskScheduler();
        Duration flushInterval = Duration.ofMillis(5000);
        boolean jitterEnabled = false;
        List<CachedStatisticsDao> daoList = createMockDaoList(3);

        AvgMaxLinkScheduler scheduler = new AvgMaxLinkScheduler(
                taskScheduler,
                flushInterval,
                jitterEnabled,
                daoList
        );

        // Should not throw exception
        scheduler.linkScheduling();
        
        assertThat(scheduler).isNotNull();
    }

    @Test
    public void testSchedulerWithJitter() {
        TaskScheduler taskScheduler = createTaskScheduler();
        Duration flushInterval = Duration.ofMillis(5000);
        boolean jitterEnabled = true;
        List<CachedStatisticsDao> daoList = createMockDaoList(3);

        AvgMaxLinkScheduler scheduler = new AvgMaxLinkScheduler(
                taskScheduler,
                flushInterval,
                jitterEnabled,
                daoList
        );

        // Should not throw exception
        scheduler.linkScheduling();
        
        assertThat(scheduler).isNotNull();
    }

    @Test
    public void testSchedulerWithJitterDefaultMaxMillis() {
        TaskScheduler taskScheduler = createTaskScheduler();
        Duration flushInterval = Duration.ofMillis(5000);
        boolean jitterEnabled = true;
        List<CachedStatisticsDao> daoList = createMockDaoList(3);

        AvgMaxLinkScheduler scheduler = new AvgMaxLinkScheduler(
                taskScheduler,
                flushInterval,
                jitterEnabled,
                daoList
        );

        // Should not throw exception
        scheduler.linkScheduling();
        
        assertThat(scheduler).isNotNull();
    }

    private TaskScheduler createTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(3);
        scheduler.setThreadNamePrefix("Test-");
        scheduler.setDaemon(true);
        scheduler.initialize();
        return scheduler;
    }

    private List<CachedStatisticsDao> createMockDaoList(int count) {
        List<CachedStatisticsDao> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            list.add(mock(CachedStatisticsDao.class));
        }
        return list;
    }
}
