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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.NonNull;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionOperations;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Deletes the Spring Batch meta data of the job instances older than their retention
 * ({@link BatchMetaCleanupProperties}).
 * <ul>
 *     <li>Candidates come from {@link BatchMetaCandidateDao} in {@code JOB_EXECUTION_ID} order, page by
 *     page; the scan stops at the first page that starts younger than the shortest retention, so a daily
 *     run reads about a day's worth of executions.</li>
 *     <li>An instance is deleted when its job is allowed (the configured names, or the jobs registered in
 *     this application), none of its executions is running and all of them are older than the job's
 *     retention. Instances with one execution (all pinpoint jobs) are decided from the page row; only
 *     restarted instances are read back through the repository. The deletion then happens in its own
 *     transaction with the instance row locked and the executions re-checked ({@link #deleteLocked}), so
 *     a restart racing with the cleanup can neither lose its new execution nor leave a half-deleted
 *     instance. {@link JobRepository#deleteJobInstance} removes the executions, their parameters and
 *     contexts, the step executions and their contexts, and the instance.</li>
 *     <li>An {@link OptimisticLockingFailureException} means the instance moved meanwhile (every DELETE
 *     checks {@code VERSION}): it is skipped. Other failures are counted and, after
 *     {@value #MAX_CONSECUTIVE_FAILURES} in a row, fail the step.</li>
 *     <li>The run stops after {@code max-instances-per-run} deletions; the next run continues.</li>
 * </ul>
 * In dry-run mode (the {@code dryRun} job parameter, default from the properties) nothing is deleted
 * and the summary reports what would have been.
 */
public class BatchMetaCleanupTasklet implements Tasklet {

    public static final String DRY_RUN_PARAMETER = "dryRun";
    static final int MAX_CONSECUTIVE_FAILURES = 100;
    /** a run of tens of thousands of instances takes minutes: say where it is */
    static final int PROGRESS_EVERY_PAGES = 20;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final JobRepository jobRepository;
    private final BatchMetaCandidateDao candidateDao;
    private final BatchMetaCleanupProperties properties;
    private final TransactionOperations transaction;
    private final Supplier<Collection<String>> registeredJobNames;
    private final Clock clock;

    // running totals for metrics
    private final AtomicLong totalDeleted = new AtomicLong();
    private final AtomicLong totalWouldDelete = new AtomicLong();
    private final AtomicLong totalSkippedRetained = new AtomicLong();
    private final AtomicLong totalSkippedRunning = new AtomicLong();
    private final AtomicLong totalSkippedNotAllowed = new AtomicLong();
    private final AtomicLong totalSkippedLocked = new AtomicLong();
    private final AtomicLong totalFailed = new AtomicLong();
    private final AtomicLong lastRunDurationMillis = new AtomicLong();
    private final AtomicBoolean lastRunCapReached = new AtomicBoolean();

    /**
     * @param transaction        transactions of the meta data source (the one the JobRepository uses), one per
     *                           deleted instance
     * @param registeredJobNames the jobs of this application ({@code JobRegistry::getJobNames}), the
     *                           allowed jobs when {@code job-names} is empty
     */
    public BatchMetaCleanupTasklet(JobRepository jobRepository,
                                   BatchMetaCandidateDao candidateDao,
                                   BatchMetaCleanupProperties properties,
                                   TransactionOperations transaction,
                                   Supplier<Collection<String>> registeredJobNames,
                                   Clock clock) {
        this.jobRepository = Objects.requireNonNull(jobRepository, "jobRepository");
        this.candidateDao = Objects.requireNonNull(candidateDao, "candidateDao");
        this.properties = Objects.requireNonNull(properties, "properties");
        this.transaction = Objects.requireNonNull(transaction, "transaction");
        this.registeredJobNames = Objects.requireNonNull(registeredJobNames, "registeredJobNames");
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    @Override
    public RepeatStatus execute(@NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) {
        final boolean dryRun = isDryRun(chunkContext);
        final Summary summary = run(dryRun);
        contribution.incrementReadCount();
        contribution.incrementWriteCount(summary.deleted());
        logger.info("{} {}", dryRun ? "DRY-RUN" : "DONE", summary);
        return RepeatStatus.FINISHED;
    }

    boolean isDryRun(ChunkContext chunkContext) {
        final Object parameter = chunkContext.getStepContext().getJobParameters().get(DRY_RUN_PARAMETER);
        if (parameter == null) {
            return properties.isDryRun();
        }
        return Boolean.parseBoolean(String.valueOf(parameter));
    }

    /**
     * One run. The summary is recorded for the metrics exactly once, also when the run fails after some
     * deletions: those are committed, and the failures count.
     */
    public Summary run(boolean dryRun) {
        final long started = System.currentTimeMillis();
        final Counters counters = new Counters();
        final Summary summary;
        boolean completed = false;
        try {
            scan(dryRun, counters, started);
            completed = true;
        } finally {
            summary = counters.summary(dryRun, System.currentTimeMillis() - started);
            record(summary);
            if (!completed) {
                logger.warn("ABORTED {}", summary);
            }
        }
        return summary;
    }

    private void scan(boolean dryRun, Counters counters, long started) {
        final LocalDateTime now = LocalDateTime.now(clock);
        final LocalDateTime youngestDeletable = now.minus(properties.minRetention());
        final Set<String> allowed = allowedJobNames();
        final int pageSize = properties.getPageSize();
        final int cap = properties.getMaxInstancesPerRun();

        final Set<Long> seenInstances = new HashSet<>();
        int consecutiveFailures = 0;
        boolean more = true;

        while (more && !counters.capReached) {
            final List<JobExecutionRow> page = candidateDao.nextPage(counters.lastExecutionId, pageSize);
            if (page.isEmpty()) {
                break;
            }
            counters.readExecutions += page.size();
            counters.pages++;
            if (counters.pages % PROGRESS_EVERY_PAGES == 0) {
                logger.info("batchMetaCleanup progress: pages={} readExecutions={} instances={} deleted={} wouldDelete={} lastExecutionId={} elapsed={}",
                        counters.pages, counters.readExecutions, counters.instances, counters.deleted, counters.wouldDelete,
                        counters.lastExecutionId, Duration.ofMillis(System.currentTimeMillis() - started));
            }
            more = page.size() == pageSize;
            counters.lastExecutionId = page.get(page.size() - 1).executionId();
            if (isYoungerThan(page.get(0).createTime(), youngestDeletable)) {
                // every following execution is younger: nothing left to delete this run
                more = false;
            }

            for (Map.Entry<Long, JobExecutionRow> entry : instancesOf(page).entrySet()) {
                if (!seenInstances.add(entry.getKey())) {
                    continue;
                }
                final JobExecutionRow row = entry.getValue();
                counters.instances++;
                if (!allowed.contains(row.jobName())) {
                    counters.skippedNotAllowed++;
                    continue;
                }
                final LocalDateTime cutoff = now.minus(properties.retentionOf(row.jobName()));
                if (isYoungerThan(row.createTime(), cutoff)) {
                    // this execution alone keeps the instance: no need to load the others
                    counters.skippedRetained++;
                    continue;
                }
                final JobInstance instance = jobInstance(row);
                final Decision decision = row.executionCount() <= 1 ? decideFromRow(row) : decide(instance, cutoff);
                if (decision != Decision.DELETE) {
                    counters.skip(decision);
                    continue;
                }
                if (dryRun) {
                    counters.wouldDelete++;
                } else {
                    final Decision outcome;
                    try {
                        outcome = deleteLocked(instance, cutoff);
                        consecutiveFailures = 0;
                    } catch (OptimisticLockingFailureException e) {
                        // changed under the lock (another cleanup instance): keep it
                        counters.skippedLocked++;
                        logger.debug("{} instance:{} changed meanwhile, kept", row.jobName(), row.instanceId());
                        continue;
                    } catch (RuntimeException e) {
                        counters.failed++;
                        consecutiveFailures++;
                        if (counters.failed == 1 || counters.failed % 100 == 0) {
                            logger.warn("{} instance:{} delete failed (total {})", row.jobName(), row.instanceId(), counters.failed, e);
                        }
                        if (consecutiveFailures >= MAX_CONSECUTIVE_FAILURES) {
                            throw new IllegalStateException(MAX_CONSECUTIVE_FAILURES + " consecutive delete failures, last on "
                                    + row.jobName() + " instance " + row.instanceId(), e);
                        }
                        continue;
                    }
                    if (outcome == Decision.DELETE) {
                        counters.deleted++;
                    } else {
                        // restarted, or deleted by another cleanup, between the page read and the lock
                        counters.skip(outcome);
                        logger.debug("{} instance:{} {} since the page was read, kept", row.jobName(), row.instanceId(), outcome);
                    }
                }
                if (counters.deleted + counters.wouldDelete >= cap) {
                    counters.capReached = true;
                    break;
                }
            }
        }
    }

    private static boolean isYoungerThan(LocalDateTime createTime, LocalDateTime youngestDeletable) {
        return createTime != null && !createTime.isBefore(youngestDeletable);
    }

    /** first row of each instance in the page, in page order */
    private static Map<Long, JobExecutionRow> instancesOf(List<JobExecutionRow> page) {
        final Map<Long, JobExecutionRow> instances = new LinkedHashMap<>();
        for (JobExecutionRow row : page) {
            instances.putIfAbsent(row.instanceId(), row);
        }
        return instances;
    }

    /**
     * Re-checks and deletes the instance in one transaction of the meta data source with its instance row
     * locked. The page row is a snapshot: another process may have restarted the instance since (a FAILED
     * one, with a new STARTING execution), and {@code deleteJobInstance} would read and delete that new
     * execution as well. Under the lock the restart has either happened, in which case the re-check sees
     * the running execution and keeps the instance, or waits: inserting an execution takes a shared lock
     * on the instance row for the foreign key, so it cannot commit before this transaction ends, and fails
     * afterwards because the instance is gone. Should an execution slip in anyway, the instance DELETE
     * fails on the foreign key and the whole transaction rolls back: never a partial deletion.
     *
     * @return {@link Decision#DELETE} when the instance was deleted, otherwise why it was kept
     */
    Decision deleteLocked(JobInstance instance, LocalDateTime cutoff) {
        return transaction.execute(status -> {
            if (!candidateDao.lockInstance(instance.getInstanceId())) {
                return Decision.GONE;
            }
            final Decision decision = decide(instance, cutoff);
            if (decision == Decision.DELETE) {
                jobRepository.deleteJobInstance(instance);
            }
            return decision;
        });
    }

    /**
     * The instance as the repository's own row mapper would build it: the DELETE of the instance row checks
     * {@code VERSION = ?}, and instance rows are inserted with version 0 and never updated, so a bare
     * {@code new JobInstance(id, name)} (version null) would never match.
     */
    static JobInstance jobInstance(JobExecutionRow row) {
        final JobInstance instance = new JobInstance(row.instanceId(), row.jobName());
        instance.incrementVersion();
        return instance;
    }

    enum Decision {
        DELETE, RETAINED, RUNNING, GONE
    }

    /**
     * An instance with a single execution (every pinpoint job: none is ever restarted) is decided from the page
     * row, which already carries the status and the create time: no repository round trip. The create time was
     * checked against the cutoff by the caller.
     */
    static Decision decideFromRow(JobExecutionRow row) {
        final BatchStatus status = row.status() == null ? BatchStatus.UNKNOWN : BatchStatus.valueOf(row.status());
        return status.isRunning() ? Decision.RUNNING : Decision.DELETE;
    }

    /**
     * Looks at every execution of a restarted instance (the page shows only one of them at a time): the
     * newest execution decides.
     */
    Decision decide(JobInstance instance, LocalDateTime cutoff) {
        final List<JobExecution> executions = jobRepository.findJobExecutions(instance);
        if (executions.isEmpty()) {
            return Decision.GONE;
        }
        for (JobExecution execution : executions) {
            if (execution.isRunning()) {
                return Decision.RUNNING;
            }
            final LocalDateTime createTime = execution.getCreateTime();
            if (createTime == null || !createTime.isBefore(cutoff)) {
                return Decision.RETAINED;
            }
        }
        return Decision.DELETE;
    }

    Set<String> allowedJobNames() {
        final Collection<String> names = properties.getJobNames().isEmpty() ? registeredJobNames.get() : properties.getJobNames();
        return new HashSet<>(names);
    }

    private void record(Summary summary) {
        totalDeleted.addAndGet(summary.deleted());
        totalWouldDelete.addAndGet(summary.wouldDelete());
        totalSkippedRetained.addAndGet(summary.skippedRetained());
        totalSkippedRunning.addAndGet(summary.skippedRunning());
        totalSkippedNotAllowed.addAndGet(summary.skippedNotAllowed());
        totalSkippedLocked.addAndGet(summary.skippedLocked());
        totalFailed.addAndGet(summary.failed());
        lastRunDurationMillis.set(summary.durationMillis());
        lastRunCapReached.set(summary.capReached());
    }

    /** the state of one run, summarized whatever way the run ends */
    private static final class Counters {
        int pages;
        long readExecutions;
        long instances;
        long deleted;
        long wouldDelete;
        long skippedRetained;
        long skippedRunning;
        long skippedNotAllowed;
        long skippedLocked;
        long failed;
        boolean capReached;
        long lastExecutionId;

        void skip(Decision decision) {
            switch (decision) {
                case RETAINED -> skippedRetained++;
                case RUNNING -> skippedRunning++;
                case GONE -> skippedLocked++;
                default -> throw new IllegalStateException(String.valueOf(decision));
            }
        }

        Summary summary(boolean dryRun, long durationMillis) {
            return new Summary(dryRun, pages, readExecutions, instances, deleted, wouldDelete, skippedRetained, skippedRunning,
                    skippedNotAllowed, skippedLocked, failed, capReached, lastExecutionId, durationMillis);
        }
    }

    /**
     * Result of one run.
     *
     * @param readExecutions   rows read from BATCH_JOB_EXECUTION
     * @param instances        distinct instances evaluated
     * @param deleted          instances deleted
     * @param wouldDelete      instances a dry run would have deleted
     * @param skippedRetained  instances with an execution younger than the retention
     * @param skippedRunning   instances with a running execution
     * @param skippedNotAllowed instances of jobs outside the allowed names
     * @param skippedLocked    instances that changed (or vanished) between the read and the delete
     * @param failed           deletions that threw
     * @param capReached       the run stopped at {@code max-instances-per-run}
     * @param lastExecutionId  the last execution id read
     */
    public record Summary(boolean dryRun, int pages, long readExecutions, long instances, long deleted, long wouldDelete,
                          long skippedRetained, long skippedRunning, long skippedNotAllowed, long skippedLocked,
                          long failed, boolean capReached, long lastExecutionId, long durationMillis) {
        @Override
        public String toString() {
            return String.format(Locale.ROOT, "batchMetaCleanup{dryRun=%s, pages=%d, readExecutions=%d, instances=%d, deleted=%d, wouldDelete=%d,"
                            + " skipped{retained=%d, running=%d, notAllowed=%d, locked=%d}, failed=%d, capReached=%s, lastExecutionId=%d, duration=%s}",
                    dryRun, pages, readExecutions, instances, deleted, wouldDelete, skippedRetained, skippedRunning,
                    skippedNotAllowed, skippedLocked, failed, capReached, lastExecutionId, Duration.ofMillis(durationMillis));
        }
    }

    public long getTotalDeleted() {
        return totalDeleted.get();
    }

    public long getTotalWouldDelete() {
        return totalWouldDelete.get();
    }

    public long getTotalSkippedRetained() {
        return totalSkippedRetained.get();
    }

    public long getTotalSkippedRunning() {
        return totalSkippedRunning.get();
    }

    public long getTotalSkippedNotAllowed() {
        return totalSkippedNotAllowed.get();
    }

    public long getTotalSkippedLocked() {
        return totalSkippedLocked.get();
    }

    public long getTotalFailed() {
        return totalFailed.get();
    }

    public long getLastRunDurationMillis() {
        return lastRunDurationMillis.get();
    }

    public boolean isLastRunCapReached() {
        return lastRunCapReached.get();
    }
}
