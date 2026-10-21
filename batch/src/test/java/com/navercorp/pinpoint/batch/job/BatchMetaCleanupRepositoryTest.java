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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The tasklet against a real {@link JobRepository} on an embedded H2 with the Spring Batch schema: the
 * deletion must remove every row of an instance from the six tables and leave the others alone.
 */
class BatchMetaCleanupRepositoryTest {

    private static final List<String> TABLES = List.of(
            "BATCH_JOB_INSTANCE", "BATCH_JOB_EXECUTION", "BATCH_JOB_EXECUTION_PARAMS", "BATCH_JOB_EXECUTION_CONTEXT",
            "BATCH_STEP_EXECUTION", "BATCH_STEP_EXECUTION_CONTEXT");

    private EmbeddedDatabase dataSource;
    private JobRepository jobRepository;
    private JdbcTemplate jdbcTemplate;
    private TransactionTemplate transaction;
    private final Clock clock = Clock.systemDefaultZone();

    @BeforeEach
    void setUp() throws Exception {
        dataSource = new EmbeddedDatabaseBuilder()
                .setType(EmbeddedDatabaseType.H2)
                .generateUniqueName(true)
                .addScript("classpath:/org/springframework/batch/core/schema-h2.sql")
                .build();
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(new DataSourceTransactionManager(dataSource));
        factory.setDatabaseType("H2");
        factory.afterPropertiesSet();
        jobRepository = factory.getObject();
        jdbcTemplate = new JdbcTemplate(dataSource);
        transaction = new TransactionTemplate(new DataSourceTransactionManager(dataSource));
    }

    @AfterEach
    void tearDown() {
        dataSource.shutdown();
    }

    /** a completed instance with two steps and contexts, created {@code age} ago (status as given) */
    private JobExecution instance(String jobName, Duration age, BatchStatus status) throws Exception {
        JobExecution execution = jobRepository.createJobExecution(jobName,
                new JobParametersBuilder().addDate("schedule.date", new Date(System.currentTimeMillis() - age.toMillis())).toJobParameters());
        LocalDateTime createTime = LocalDateTime.now(clock).minus(age);
        execution.setCreateTime(createTime);
        execution.setStartTime(createTime);
        execution.getExecutionContext().putString("job", "context");
        for (String stepName : List.of("step1", "step2")) {
            StepExecution step = execution.createStepExecution(stepName);
            jobRepository.add(step);
            step.getExecutionContext().putString("step", stepName);
            step.setStatus(BatchStatus.COMPLETED);
            step.setExitStatus(ExitStatus.COMPLETED);
            step.setEndTime(createTime.plusSeconds(1));
            jobRepository.updateExecutionContext(step);
            jobRepository.update(step);
        }
        execution.setStatus(status);
        if (status == BatchStatus.COMPLETED || status == BatchStatus.FAILED) {
            execution.setEndTime(createTime.plusSeconds(2));
            execution.setExitStatus(status == BatchStatus.COMPLETED ? ExitStatus.COMPLETED : ExitStatus.FAILED);
        }
        jobRepository.updateExecutionContext(execution);
        jobRepository.update(execution);
        return execution;
    }

    private Map<String, Integer> rowCounts() {
        return TABLES.stream().collect(java.util.stream.Collectors.toMap(t -> t,
                t -> jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + t, Integer.class), (a, b) -> a, java.util.LinkedHashMap::new));
    }

    private boolean instanceExists(long instanceId) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM BATCH_JOB_INSTANCE WHERE JOB_INSTANCE_ID = ?", Integer.class, instanceId) > 0;
    }

    private int instancesOf(String jobName) {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM BATCH_JOB_INSTANCE WHERE JOB_NAME = ?", Integer.class, jobName);
    }

    @Test
    void deletesTheWholeGraphOfOldInstancesAndKeepsTheRest() throws Exception {
        JobExecution oldAlarm = instance("alarmJob", Duration.ofDays(60), BatchStatus.COMPLETED);
        JobExecution oldFailed = instance("alarmJob", Duration.ofDays(45), BatchStatus.FAILED);
        JobExecution recentAlarm = instance("alarmJob", Duration.ofDays(10), BatchStatus.COMPLETED);
        JobExecution oldButRunning = instance("alarmJob", Duration.ofDays(50), BatchStatus.STARTED);
        JobExecution otherApp = instance("rumAlarmJob", Duration.ofDays(400), BatchStatus.COMPLETED);
        JobExecution oldLink = instance("otlpLinkJoinJob", Duration.ofDays(2), BatchStatus.COMPLETED);
        JobExecution recentLink = instance("otlpLinkJoinJob", Duration.ofHours(2), BatchStatus.COMPLETED);

        Map<String, Integer> before = rowCounts();
        assertThat(before.get("BATCH_JOB_INSTANCE")).isEqualTo(7);
        assertThat(before.get("BATCH_STEP_EXECUTION")).isEqualTo(14);
        assertThat(before.get("BATCH_STEP_EXECUTION_CONTEXT")).isEqualTo(14);

        BatchMetaCleanupProperties properties = new BatchMetaCleanupProperties();
        properties.setRetentionByJob(Map.of("otlpLinkJoinJob", Duration.ofDays(1)));
        properties.setPageSize(100);
        BatchMetaCleanupTasklet tasklet = new BatchMetaCleanupTasklet(jobRepository, new BatchMetaCandidateDao(dataSource), properties,
                transaction, () -> List.of("alarmJob", "otlpLinkJoinJob"), clock);

        // dry run first: nothing changes
        BatchMetaCleanupTasklet.Summary dry = tasklet.run(true);
        assertThat(dry.wouldDelete()).isEqualTo(3);
        assertThat(rowCounts()).isEqualTo(before);

        BatchMetaCleanupTasklet.Summary summary = tasklet.run(false);

        assertThat(summary.deleted()).isEqualTo(3);           // oldAlarm, oldFailed, oldLink
        assertThat(summary.skippedRunning()).isEqualTo(1);    // oldButRunning
        assertThat(summary.skippedRetained()).isEqualTo(2);   // recentAlarm, recentLink
        assertThat(summary.skippedNotAllowed()).isEqualTo(1); // rumAlarmJob
        assertThat(summary.failed()).isZero();

        Map<String, Integer> after = rowCounts();
        // 4 instances remain, each with 1 execution, 1 job context, 1 param, 2 steps, 2 step contexts
        assertThat(after.get("BATCH_JOB_INSTANCE")).isEqualTo(4);
        assertThat(after.get("BATCH_JOB_EXECUTION")).isEqualTo(4);
        assertThat(after.get("BATCH_JOB_EXECUTION_PARAMS")).isEqualTo(4);
        assertThat(after.get("BATCH_JOB_EXECUTION_CONTEXT")).isEqualTo(4);
        assertThat(after.get("BATCH_STEP_EXECUTION")).isEqualTo(8);
        assertThat(after.get("BATCH_STEP_EXECUTION_CONTEXT")).isEqualTo(8);
        assertThat(instancesOf("rumAlarmJob")).isEqualTo(1);
        assertThat(instancesOf("otlpLinkJoinJob")).isEqualTo(1);
        assertThat(instancesOf("alarmJob")).isEqualTo(2);
        for (JobExecution gone : List.of(oldAlarm, oldFailed, oldLink)) {
            assertThat(instanceExists(gone.getJobInstance().getInstanceId())).isFalse();
        }
        for (JobExecution kept : List.of(recentAlarm, oldButRunning, otherApp, recentLink)) {
            assertThat(instanceExists(kept.getJobInstance().getInstanceId())).isTrue();
        }

        // a second run finds nothing more to do
        BatchMetaCleanupTasklet.Summary again = tasklet.run(false);
        assertThat(again.deleted()).isZero();
        assertThat(rowCounts()).isEqualTo(after);
    }

    /**
     * The review scenario: a FAILED instance is picked from the page, then another process restarts it
     * before the deletion. The re-check under the instance lock sees the new STARTING execution and keeps
     * the instance with both executions; a plain deleteJobInstance would have removed them all.
     */
    @Test
    void anInstanceRestartedBetweenThePageReadAndTheDeleteIsKeptWithItsNewExecution() throws Exception {
        // distinct ages: schedule.date is the identifying parameter (second resolution in the job key), and two
        // instances with the same key would be one instance with two executions
        JobExecution victim = instance("alarmJob", Duration.ofDays(45), BatchStatus.FAILED);
        JobExecution other = instance("alarmJob", Duration.ofDays(46), BatchStatus.COMPLETED);
        JobParameters victimParameters = victim.getJobParameters();

        // the dao hands out the page, then the "other process" restarts the victim
        BatchMetaCandidateDao racingDao = new BatchMetaCandidateDao(dataSource) {
            private boolean restarted;

            @Override
            public List<JobExecutionRow> nextPage(long afterExecutionId, int limit) {
                List<JobExecutionRow> page = super.nextPage(afterExecutionId, limit);
                if (!restarted && !page.isEmpty()) {
                    restarted = true;
                    try {
                        JobExecution restart = jobRepository.createJobExecution("alarmJob", victimParameters);
                        assertThat(restart.getJobInstance().getInstanceId()).isEqualTo(victim.getJobInstance().getInstanceId());
                        assertThat(restart.getStatus()).isEqualTo(BatchStatus.STARTING);
                    } catch (Exception e) {
                        throw new IllegalStateException(e);
                    }
                }
                return page;
            }
        };
        BatchMetaCleanupProperties properties = new BatchMetaCleanupProperties();
        properties.setPageSize(100);
        BatchMetaCleanupTasklet tasklet = new BatchMetaCleanupTasklet(jobRepository, racingDao, properties, transaction,
                () -> List.of("alarmJob"), clock);

        BatchMetaCleanupTasklet.Summary summary = tasklet.run(false);

        assertThat(summary.deleted()).isEqualTo(1);          // other
        assertThat(summary.skippedRunning()).isEqualTo(1);   // victim, seen running under the lock
        assertThat(summary.failed()).isZero();
        assertThat(instanceExists(victim.getJobInstance().getInstanceId())).isTrue();
        assertThat(instanceExists(other.getJobInstance().getInstanceId())).isFalse();
        assertThat(jobRepository.findJobExecutions(victim.getJobInstance())).hasSize(2);
        // the new execution and the old one, with their steps and contexts, are intact
        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM BATCH_STEP_EXECUTION s JOIN BATCH_JOB_EXECUTION e ON e.JOB_EXECUTION_ID = s.JOB_EXECUTION_ID WHERE e.JOB_INSTANCE_ID = ?",
                Integer.class, victim.getJobInstance().getInstanceId())).isEqualTo(2);
    }

    /**
     * More eligible executions than a page (the smallest page is 100) and a cap above the page size: a run has
     * to continue past the first page with the real query, keyed on the last execution id read.
     */
    @Test
    void pagesThroughMoreInstancesThanThePageSize() throws Exception {
        final int old = 130;
        final int recent = 5;
        for (int i = 0; i < old; i++) {
            instance("alarmJob", Duration.ofDays(40).plusMinutes(i), BatchStatus.COMPLETED);
        }
        for (int i = 0; i < recent; i++) {
            instance("alarmJob", Duration.ofDays(1 + i), BatchStatus.COMPLETED);
        }
        BatchMetaCleanupProperties properties = new BatchMetaCleanupProperties();
        properties.setPageSize(100);
        properties.setMaxInstancesPerRun(120);
        BatchMetaCleanupTasklet tasklet = new BatchMetaCleanupTasklet(jobRepository, new BatchMetaCandidateDao(dataSource), properties,
                transaction, () -> List.of("alarmJob"), clock);

        // first run: the whole first page and part of the second, until the cap
        BatchMetaCleanupTasklet.Summary first = tasklet.run(false);
        assertThat(first.pages()).isEqualTo(2);
        assertThat(first.readExecutions()).isEqualTo(old + recent);
        assertThat(first.deleted()).isEqualTo(120);
        assertThat(first.capReached()).isTrue();
        assertThat(instancesOf("alarmJob")).isEqualTo(old + recent - 120);

        // second run: the ten old ones left and the recent ones fit in one page
        BatchMetaCleanupTasklet.Summary second = tasklet.run(false);
        assertThat(second.pages()).isEqualTo(1);
        assertThat(second.readExecutions()).isEqualTo(old - 120 + recent);
        assertThat(second.deleted()).isEqualTo(old - 120);
        assertThat(second.skippedRetained()).isEqualTo(recent);
        assertThat(second.capReached()).isFalse();

        // third run: only the recent ones remain, nothing to do
        BatchMetaCleanupTasklet.Summary third = tasklet.run(false);
        assertThat(third.deleted()).isZero();
        assertThat(third.readExecutions()).isEqualTo(recent);
        assertThat(instancesOf("alarmJob")).isEqualTo(recent);
        assertThat(tasklet.getTotalDeleted()).isEqualTo(old);
        // every row of the deleted instances is gone: the remaining ones have 1 execution, 1 param, 1 context, 2 steps each
        Map<String, Integer> after = rowCounts();
        assertThat(after.get("BATCH_JOB_EXECUTION")).isEqualTo(recent);
        assertThat(after.get("BATCH_JOB_EXECUTION_PARAMS")).isEqualTo(recent);
        assertThat(after.get("BATCH_JOB_EXECUTION_CONTEXT")).isEqualTo(recent);
        assertThat(after.get("BATCH_STEP_EXECUTION")).isEqualTo(recent * 2);
        assertThat(after.get("BATCH_STEP_EXECUTION_CONTEXT")).isEqualTo(recent * 2);
    }
}
