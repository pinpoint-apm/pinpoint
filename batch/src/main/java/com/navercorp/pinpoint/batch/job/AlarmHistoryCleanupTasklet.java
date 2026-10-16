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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * Deletes alarm history older than the expiration, a chunk per call. The sent and dead
 * outbox rows of a history go first, because a history is deleted only when no outbox
 * row points at it. A pending outbox row keeps its history until it is sent or dead.
 */
public class AlarmHistoryCleanupTasklet implements Tasklet {

    private static final String HISTORY_DELETED_KEY = "alarmHistoryCleanup.historyDeleted";
    private static final String OUTBOX_DELETED_KEY = "alarmHistoryCleanup.outboxDeleted";

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AlarmNotificationOutboxDao outboxDao;
    private final AlarmHistoryV2Dao historyDao;
    private final int expirationDays;
    private final int chunkSize;

    public AlarmHistoryCleanupTasklet(AlarmNotificationOutboxDao outboxDao,
                                      AlarmHistoryV2Dao historyDao,
                                      int expirationDays,
                                      int chunkSize) {
        this.outboxDao = Objects.requireNonNull(outboxDao, "outboxDao");
        this.historyDao = Objects.requireNonNull(historyDao, "historyDao");
        if (expirationDays <= 0) {
            throw new IllegalArgumentException("expirationDays must be greater than 0");
        }
        if (chunkSize <= 0) {
            throw new IllegalArgumentException("chunkSize must be greater than 0");
        }
        this.expirationDays = expirationDays;
        this.chunkSize = chunkSize;
    }

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) {
        StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();
        ExecutionContext context = stepExecution.getExecutionContext();
        LocalDateTime threshold = threshold(stepExecution.getJobExecution());

        int deletedOutbox = outboxDao.deleteTerminalOlderThan(threshold, chunkSize);
        int deletedHistory = historyDao.deleteOlderThan(threshold, chunkSize);
        long outboxTotal = context.getLong(OUTBOX_DELETED_KEY, 0) + deletedOutbox;
        long historyTotal = context.getLong(HISTORY_DELETED_KEY, 0) + deletedHistory;
        context.putLong(OUTBOX_DELETED_KEY, outboxTotal);
        context.putLong(HISTORY_DELETED_KEY, historyTotal);

        if (deletedOutbox > 0 || deletedHistory > 0) {
            return RepeatStatus.CONTINUABLE;
        }
        logger.info("Alarm history cleanup completed. deleted history={}, outbox={} older than {} ({} days)",
                historyTotal, outboxTotal, threshold, expirationDays);
        return RepeatStatus.FINISHED;
    }

    private LocalDateTime threshold(JobExecution jobExecution) {
        Instant scheduleTime = Instant.ofEpochMilli(JobParametersUtils.getScheduleTime(jobExecution));
        // created_at is written in UTC, so the threshold compared against it has to be too.
        return LocalDateTime.ofInstant(scheduleTime, ZoneOffset.UTC).minusDays(expirationDays);
    }
}
