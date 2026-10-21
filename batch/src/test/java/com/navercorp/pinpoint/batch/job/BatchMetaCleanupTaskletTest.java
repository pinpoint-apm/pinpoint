/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.batch.job;

import com.navercorp.pinpoint.batch.common.BatchMetaCleanupProperties;
import com.navercorp.pinpoint.batch.dao.BatchMetaCandidateDao;
import com.navercorp.pinpoint.batch.dao.BatchMetaCandidateDao.JobExecutionRow;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionOperations;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class BatchMetaCleanupTaskletTest {

    private static final ZoneId ZONE = ZoneId.systemDefault();
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-20T10:00:00Z"), ZONE);
    private final LocalDateTime now = LocalDateTime.now(clock);

    private final JobRepository jobRepository = mock(JobRepository.class);
    private final BatchMetaCandidateDao dao = mock(BatchMetaCandidateDao.class);
    private final BatchMetaCleanupProperties properties = new BatchMetaCleanupProperties();
    private final List<String> registered = new ArrayList<>(List.of("alarmJob", "agentCountJob", "otlpLinkJoinJob"));

    /** the rows the dao serves, in id order; the mock pages through them */
    private final List<JobExecutionRow> rows = new ArrayList<>();
    /** executions per instance id, as the repository returns them */
    private final Map<Long, List<JobExecution>> executions = new HashMap<>();
    private final List<Long> deleted = new ArrayList<>();

    @BeforeEach
    void setUp() {
        properties.setPageSize(100);
        when(dao.lockInstance(anyLong())).thenReturn(true);
        when(dao.nextPage(anyLong(), anyInt())).thenAnswer(invocation -> {
            long after = invocation.getArgument(0);
            int limit = invocation.getArgument(1);
            List<JobExecutionRow> page = new ArrayList<>();
            for (JobExecutionRow row : rows) {
                if (row.executionId() > after && page.size() < limit) {
                    page.add(row);
                }
            }
            return page;
        });
        when(jobRepository.findJobExecutions(any(JobInstance.class))).thenAnswer(invocation -> {
            JobInstance instance = invocation.getArgument(0);
            return executions.getOrDefault(instance.getInstanceId(), List.of());
        });
        org.mockito.Mockito.doAnswer(invocation -> {
            JobInstance instance = invocation.getArgument(0);
            // the instance DELETE checks VERSION = ?; instance rows are stored with version 0
            assertThat(instance.getVersion()).isZero();
            deleted.add(instance.getInstanceId());
            return null;
        }).when(jobRepository).deleteJobInstance(any(JobInstance.class));
    }

    private BatchMetaCleanupTasklet tasklet() {
        return new BatchMetaCleanupTasklet(jobRepository, dao, properties, TransactionOperations.withoutTransaction(), () -> registered, clock);
    }

    /** one execution per instance, ids = instance id; status COMPLETED unless given */
    private void instance(long id, String jobName, Duration age) {
        instance(id, jobName, age, BatchStatus.COMPLETED);
    }

    private void instance(long id, String jobName, Duration age, BatchStatus status) {
        LocalDateTime createTime = now.minus(age);
        rows.add(new JobExecutionRow(id, id, jobName, createTime, status.name(), 1));
        JobExecution execution = new JobExecution(new JobInstance(id, jobName), id, new JobParameters());
        execution.setCreateTime(createTime);
        execution.setStatus(status);
        executions.computeIfAbsent(id, k -> new ArrayList<>()).add(execution);
    }

    /** a restart of instance {@code id} with its own execution id and age */
    private void restart(long id, long executionId, Duration age) {
        JobExecutionRow first = rows.stream().filter(r -> r.instanceId() == id).findFirst().orElseThrow();
        LocalDateTime createTime = now.minus(age);
        rows.add(new JobExecutionRow(executionId, id, first.jobName(), createTime, "COMPLETED", 2));
        // the first row of the instance now also reports two executions
        rows.replaceAll(r -> r.instanceId() == id && r.executionCount() == 1
                ? new JobExecutionRow(r.executionId(), r.instanceId(), r.jobName(), r.createTime(), r.status(), 2) : r);
        JobExecution execution = new JobExecution(new JobInstance(id, first.jobName()), executionId, new JobParameters());
        execution.setCreateTime(createTime);
        execution.setStatus(BatchStatus.COMPLETED);
        executions.get(id).add(execution);
    }

    private static ChunkContext chunkContext(Map<String, Object> jobParameters) {
        org.springframework.batch.core.JobParametersBuilder builder = new org.springframework.batch.core.JobParametersBuilder();
        jobParameters.forEach((k, v) -> builder.addString(k, String.valueOf(v)));
        JobExecution jobExecution = new JobExecution(1L, builder.toJobParameters());
        return new ChunkContext(new StepContext(new StepExecution("batchMetaCleanupStep", jobExecution)));
    }

    @Test
    void deletesInstancesOlderThanTheRetentionOfTheirJob() {
        properties.setRetentionByJob(new LinkedHashMap<>(Map.of("otlpLinkJoinJob", Duration.ofDays(1))));
        instance(1, "alarmJob", Duration.ofDays(31));          // deleted (30d default)
        instance(2, "alarmJob", Duration.ofDays(29));          // kept
        instance(3, "otlpLinkJoinJob", Duration.ofDays(2));    // deleted (1d)
        instance(4, "otlpLinkJoinJob", Duration.ofHours(23));  // kept
        instance(5, "agentCountJob", Duration.ofDays(30).plusMinutes(1)); // deleted, just past the cutoff

        BatchMetaCleanupTasklet.Summary summary = tasklet().run(false);

        assertThat(deleted).containsExactly(1L, 3L, 5L);
        assertThat(summary.deleted()).isEqualTo(3);
        assertThat(summary.skippedRetained()).isEqualTo(2);
        assertThat(summary.instances()).isEqualTo(5);
        assertThat(summary.capReached()).isFalse();
        // single-execution instances are decided from the page rows; only the three deletions re-check under the lock
        verify(jobRepository, times(3)).findJobExecutions(any(JobInstance.class));
        verify(dao, times(3)).lockInstance(anyLong());
    }

    @Test
    void keepsRunningInstancesAndRestartedInstancesWithARecentExecution() {
        instance(1, "alarmJob", Duration.ofDays(40), BatchStatus.STARTED);
        instance(2, "alarmJob", Duration.ofDays(40));
        restart(2, 20, Duration.ofDays(3));
        instance(3, "alarmJob", Duration.ofDays(40), BatchStatus.FAILED);
        restart(3, 30, Duration.ofDays(35));

        BatchMetaCleanupTasklet.Summary summary = tasklet().run(false);

        // 1 is running, 2 was restarted 3 days ago, 3's executions are both old
        assertThat(deleted).containsExactly(3L);
        assertThat(summary.skippedRunning()).isEqualTo(1);
        assertThat(summary.skippedRetained()).isEqualTo(1);
        // the instance is evaluated once although it appears twice in the pages
        assertThat(summary.instances()).isEqualTo(3);
        // the restarted instances (2 and 3) were read back through the repository, 3 once more under the lock
        verify(jobRepository, times(3)).findJobExecutions(any(JobInstance.class));
    }

    @Test
    void onlyTheRegisteredJobsAreTouchedUnlessNamesAreConfigured() {
        instance(1, "rumAlarmJob", Duration.ofDays(400));   // another application's job
        instance(2, "alarmJob", Duration.ofDays(400));

        BatchMetaCleanupTasklet.Summary summary = tasklet().run(false);
        assertThat(deleted).containsExactly(2L);
        assertThat(summary.skippedNotAllowed()).isEqualTo(1);

        deleted.clear();
        properties.setJobNames(List.of("rumAlarmJob"));
        summary = tasklet().run(false);
        assertThat(deleted).containsExactly(1L);
        assertThat(summary.skippedNotAllowed()).isEqualTo(1);
    }

    @Test
    void anInstanceRestartedAfterThePageWasReadIsKept() {
        instance(1, "alarmJob", Duration.ofDays(40), BatchStatus.FAILED);
        instance(2, "alarmJob", Duration.ofDays(40));
        // between the page read and the lock, another process restarted instance 1: a new STARTING execution
        JobExecution restarted = new JobExecution(new JobInstance(1L, "alarmJob"), 10L, new JobParameters());
        restarted.setCreateTime(now);
        restarted.setStatus(BatchStatus.STARTING);
        executions.get(1L).add(restarted);

        BatchMetaCleanupTasklet.Summary summary = tasklet().run(false);

        assertThat(deleted).containsExactly(2L);
        assertThat(summary.skippedRunning()).isEqualTo(1);
        assertThat(summary.deleted()).isEqualTo(1);
    }

    @Test
    void anInstanceDeletedByAnotherCleanupBeforeTheLockIsCounted() {
        instance(1, "alarmJob", Duration.ofDays(40));
        when(dao.lockInstance(1L)).thenReturn(false);

        BatchMetaCleanupTasklet.Summary summary = tasklet().run(false);

        assertThat(deleted).isEmpty();
        assertThat(summary.skippedLocked()).isEqualTo(1);
        verify(jobRepository, never()).deleteJobInstance(any());
    }

    @Test
    void dryRunDeletesNothingAndCountsWhatItWouldDelete() {
        instance(1, "alarmJob", Duration.ofDays(40));
        instance(2, "alarmJob", Duration.ofDays(40));

        BatchMetaCleanupTasklet.Summary summary = tasklet().run(true);

        verify(jobRepository, never()).deleteJobInstance(any());
        verify(dao, never()).lockInstance(anyLong());
        assertThat(summary.wouldDelete()).isEqualTo(2);
        assertThat(summary.deleted()).isZero();
    }

    @Test
    void theDryRunJobParameterOverridesTheProperty() {
        BatchMetaCleanupTasklet tasklet = tasklet();
        properties.setDryRun(true);
        assertThat(tasklet.isDryRun(chunkContext(Map.of()))).isTrue();
        assertThat(tasklet.isDryRun(chunkContext(Map.of("dryRun", "false")))).isFalse();
        properties.setDryRun(false);
        assertThat(tasklet.isDryRun(chunkContext(Map.of()))).isFalse();
        assertThat(tasklet.isDryRun(chunkContext(Map.of("dryRun", "true")))).isTrue();
    }

    @Test
    void stopsAtTheCapAndTheNextRunContinues() {
        properties.setMaxInstancesPerRun(2);
        for (long id = 1; id <= 5; id++) {
            instance(id, "alarmJob", Duration.ofDays(40));
        }

        BatchMetaCleanupTasklet.Summary first = tasklet().run(false);
        assertThat(first.deleted()).isEqualTo(2);
        assertThat(first.capReached()).isTrue();
        assertThat(deleted).containsExactly(1L, 2L);

        // the deleted rows are gone from the table
        rows.removeIf(r -> deleted.contains(r.instanceId()));
        BatchMetaCleanupTasklet.Summary second = tasklet().run(false);
        assertThat(second.deleted()).isEqualTo(2);
        assertThat(deleted).containsExactly(1L, 2L, 3L, 4L);
    }

    @Test
    void theScanStopsAtTheFirstPageYoungerThanTheShortestRetention() {
        properties.setPageSize(100);
        // 150 old executions, then 300 recent ones: the third page starts inside the retention
        for (long id = 1; id <= 150; id++) {
            instance(id, "alarmJob", Duration.ofDays(40));
        }
        for (long id = 151; id <= 450; id++) {
            instance(id, "alarmJob", Duration.ofDays(10));
        }

        BatchMetaCleanupTasklet.Summary summary = tasklet().run(false);

        assertThat(summary.deleted()).isEqualTo(150);
        // pages 1-3 are read (page 2 still starts with old rows), page 4 and 5 are not
        assertThat(summary.pages()).isEqualTo(3);
        assertThat(summary.readExecutions()).isEqualTo(300);
        verify(dao, times(3)).nextPage(anyLong(), anyInt());
    }

    @Test
    void aChangedInstanceIsSkippedAndRepeatedFailuresFailTheStep() {
        for (long id = 1; id <= 3; id++) {
            instance(id, "alarmJob", Duration.ofDays(40));
        }
        JobInstance changed = new JobInstance(2L, "alarmJob");
        changed.incrementVersion();
        doThrow(new OptimisticLockingFailureException("changed")).when(jobRepository).deleteJobInstance(changed);

        BatchMetaCleanupTasklet.Summary summary = tasklet().run(false);
        assertThat(deleted).containsExactly(1L, 3L);
        assertThat(summary.skippedLocked()).isEqualTo(1);
        assertThat(summary.failed()).isZero();

        rows.clear();
        executions.clear();
        deleted.clear();
        for (long id = 100; id < 100 + BatchMetaCleanupTasklet.MAX_CONSECUTIVE_FAILURES + 10; id++) {
            instance(id, "alarmJob", Duration.ofDays(40));
        }
        doThrow(new IllegalStateException("db down")).when(jobRepository).deleteJobInstance(any(JobInstance.class));
        assertThatThrownBy(() -> tasklet().run(false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("consecutive");
    }

    /**
     * The database goes away after some deletions: the run fails, but the deletions it committed and the
     * failures it saw are recorded for the metrics, and so is the duration of the aborted run.
     */
    @Test
    void aFailedRunStillRecordsItsCommittedDeletionsAndFailures() {
        final long committed = 3;
        for (long id = 1; id <= committed + BatchMetaCleanupTasklet.MAX_CONSECUTIVE_FAILURES + 10; id++) {
            instance(id, "alarmJob", Duration.ofDays(40));
        }
        org.mockito.Mockito.doAnswer(invocation -> {
            JobInstance instance = invocation.getArgument(0);
            if (instance.getInstanceId() > committed) {
                throw new IllegalStateException("db down");
            }
            deleted.add(instance.getInstanceId());
            return null;
        }).when(jobRepository).deleteJobInstance(any(JobInstance.class));
        BatchMetaCleanupTasklet tasklet = tasklet();

        assertThatThrownBy(() -> tasklet.run(false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("consecutive");

        assertThat(deleted).containsExactly(1L, 2L, 3L);
        assertThat(tasklet.getTotalDeleted()).isEqualTo(committed);
        assertThat(tasklet.getTotalFailed()).isEqualTo(BatchMetaCleanupTasklet.MAX_CONSECUTIVE_FAILURES);
        assertThat(tasklet.getLastRunDurationMillis()).isNotNegative();
        assertThat(tasklet.isLastRunCapReached()).isFalse();

        // a later run that completes records on top, once
        rows.clear();
        executions.clear();
        instance(1_000, "alarmJob", Duration.ofDays(40));
        org.mockito.Mockito.doAnswer(invocation -> {
            deleted.add(((JobInstance) invocation.getArgument(0)).getInstanceId());
            return null;
        }).when(jobRepository).deleteJobInstance(any(JobInstance.class));
        assertThat(tasklet.run(false).deleted()).isEqualTo(1);
        assertThat(tasklet.getTotalDeleted()).isEqualTo(committed + 1);
        assertThat(tasklet.getTotalFailed()).isEqualTo(BatchMetaCleanupTasklet.MAX_CONSECUTIVE_FAILURES);
    }

    @Test
    void executeReportsTheWriteCount() {
        instance(1, "alarmJob", Duration.ofDays(40));
        StepContribution contribution = new StepContribution(new StepExecution("batchMetaCleanupStep", new JobExecution(1L, new JobParameters())));

        tasklet().execute(contribution, chunkContext(Map.of("dryRun", "false")));

        assertThat(contribution.getWriteCount()).isEqualTo(1);
        assertThat(deleted).containsExactly(1L);
    }
}
