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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.TaskScheduler;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class StatisticsLinkSchedulerTest {

    private static final Duration INTERVAL = Duration.ofSeconds(5);

    private final TaskScheduler taskScheduler = mock(TaskScheduler.class);
    private final StartTimeDistributor startTimeDistributor = mock(StartTimeDistributor.class);
    private final CachedStatisticsDao dao1 = mock(CachedStatisticsDao.class);
    private final CachedStatisticsDao dao2 = mock(CachedStatisticsDao.class);
    private final ScheduledFuture<?> flushTask = mock(ScheduledFuture.class);

    private StatisticsLinkScheduler newScheduler() {
        doReturn(flushTask).when(taskScheduler)
                .scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), eq(INTERVAL));
        return new StatisticsLinkScheduler(taskScheduler, INTERVAL, startTimeDistributor, List.of(dao1, dao2));
    }

    @Test
    void startSchedulesTheFlushOfEachDao() {
        StatisticsLinkScheduler scheduler = newScheduler();

        // not scheduled by the constructor
        verify(taskScheduler, never()).scheduleWithFixedDelay(any(Runnable.class), any(Instant.class), any(Duration.class));
        assertThat(scheduler.isRunning()).isFalse();
        assertThat(scheduler.isAutoStartup()).isTrue();

        scheduler.start();
        assertThat(scheduler.isRunning()).isTrue();

        ArgumentCaptor<Runnable> flush = ArgumentCaptor.forClass(Runnable.class);
        verify(taskScheduler, times(2)).scheduleWithFixedDelay(flush.capture(), any(Instant.class), eq(INTERVAL));
        verifyNoInteractions(dao1, dao2);
        for (Runnable runnable : flush.getAllValues()) {
            runnable.run();
        }
        verify(dao1).flushLink();
        verify(dao2).flushLink();
        verify(dao1, never()).flushAvgMax();
        verify(dao2, never()).flushAvgMax();
    }

    @Test
    void stopCancelsTheFlushAndFlushesTheRest() {
        StatisticsLinkScheduler scheduler = newScheduler();
        scheduler.start();

        scheduler.stop();

        assertThat(scheduler.isRunning()).isFalse();
        verify(flushTask, times(2)).cancel(false);
        verify(dao1).flushLink();
        verify(dao2).flushLink();
    }

    @Test
    void failedLastFlushDoesNotStopTheOthers() {
        StatisticsLinkScheduler scheduler = newScheduler();
        doThrow(new RuntimeException("flush failed")).when(dao1).flushLink();
        scheduler.start();

        scheduler.stop();

        assertThat(scheduler.isRunning()).isFalse();
        verify(dao2).flushLink();
    }

    @Test
    void stopsAfterTheDefaultPhase() {
        StatisticsLinkScheduler scheduler = newScheduler();

        // a higher phase stops first: the receivers (DEFAULT_PHASE) stop before the last flush
        assertThat(scheduler.getPhase()).isLessThan(SmartLifecycle.DEFAULT_PHASE);
    }
}
