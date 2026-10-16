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

package com.navercorp.pinpoint.batch.job;

import com.navercorp.pinpoint.alarm.dao.AlarmHistoryV2Dao;
import com.navercorp.pinpoint.alarm.dao.AlarmNotificationOutboxDao;
import com.navercorp.pinpoint.batch.util.JobParametersUtils;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AlarmHistoryCleanupTaskletTest {

    private static final LocalDateTime SCHEDULE_TIME = LocalDateTime.of(2026, 10, 16, 3, 30);

    private final AlarmNotificationOutboxDao outboxDao = mock(AlarmNotificationOutboxDao.class);
    private final AlarmHistoryV2Dao historyDao = mock(AlarmHistoryV2Dao.class);

    @Test
    void deletesOutboxBeforeHistoryUntilNothingIsLeft() {
        when(outboxDao.deleteTerminalOlderThan(any(), eq(100))).thenReturn(3, 0);
        when(historyDao.deleteOlderThan(any(), eq(100))).thenReturn(2, 0);
        AlarmHistoryCleanupTasklet tasklet = new AlarmHistoryCleanupTasklet(outboxDao, historyDao, 14, 100);
        ChunkContext chunkContext = chunkContext();

        assertThat(tasklet.execute(null, chunkContext)).isEqualTo(RepeatStatus.CONTINUABLE);
        assertThat(tasklet.execute(null, chunkContext)).isEqualTo(RepeatStatus.FINISHED);

        InOrder inOrder = inOrder(outboxDao, historyDao);
        inOrder.verify(outboxDao).deleteTerminalOlderThan(any(), eq(100));
        inOrder.verify(historyDao).deleteOlderThan(any(), eq(100));
    }

    @Test
    void continuesWhileOnlyHistoryIsDeleted() {
        when(historyDao.deleteOlderThan(any(), eq(100))).thenReturn(1, 0);
        AlarmHistoryCleanupTasklet tasklet = new AlarmHistoryCleanupTasklet(outboxDao, historyDao, 14, 100);
        ChunkContext chunkContext = chunkContext();

        assertThat(tasklet.execute(null, chunkContext)).isEqualTo(RepeatStatus.CONTINUABLE);
        assertThat(tasklet.execute(null, chunkContext)).isEqualTo(RepeatStatus.FINISHED);
    }

    @Test
    void deletesOlderThanTheExpirationBeforeTheScheduleTimeInUtc() {
        AlarmHistoryCleanupTasklet tasklet = new AlarmHistoryCleanupTasklet(outboxDao, historyDao, 14, 100);

        tasklet.execute(null, chunkContext());

        LocalDateTime expected = SCHEDULE_TIME.minusDays(14);
        verify(outboxDao).deleteTerminalOlderThan(expected, 100);
        verify(historyDao).deleteOlderThan(expected, 100);
    }

    @Test
    void rejectsNonPositiveSettings() {
        assertThatThrownBy(() -> new AlarmHistoryCleanupTasklet(outboxDao, historyDao, 0, 100))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new AlarmHistoryCleanupTasklet(outboxDao, historyDao, 14, 0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static ChunkContext chunkContext() {
        Date scheduleDate = Date.from(SCHEDULE_TIME.toInstant(ZoneOffset.UTC));
        JobExecution jobExecution = new JobExecution(1L, new JobParametersBuilder()
                .addDate(JobParametersUtils.SCHEDULE_DATE_KEY, scheduleDate)
                .toJobParameters());
        StepExecution stepExecution = new StepExecution("alarmHistoryCleanupStep", jobExecution);
        return new ChunkContext(new StepContext(stepExecution));
    }
}
