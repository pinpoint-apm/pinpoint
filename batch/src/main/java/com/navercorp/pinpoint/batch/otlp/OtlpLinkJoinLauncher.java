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

package com.navercorp.pinpoint.batch.otlp;

import com.navercorp.pinpoint.otlp.trace.linkindex.LinkIndexKeys;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongSupplier;

/**
 * Launches {@code otlpLinkJoinJob} for the slots that have ended, in slot order.
 * <ul>
 *     <li>The slot id is the only identifying job parameter, so the JobRepository completes each slot
 *     once, even with several batch instances, and a FAILED slot is restarted (completed steps are
 *     skipped) by the next run.</li>
 *     <li>The cleanup of slot k deletes the keys of k-1, which the job of k-1 reads, so a slot never starts
 *     before the previous one has completed: on failure the run stops and every following run retries from
 *     that slot, however long ago it ended, until its input has expired. The cleanup step checks the same
 *     condition in the JobRepository against the jobs of other instances
 *     ({@link OtlpLinkJoinTasklets.Cleanup}).</li>
 *     <li>The job of slot k reads k-1 and k, and the keys of k-1 are the first to expire, at
 *     {@code slot start of k + ttl} ({@link #expired}). A slot past that deadline is skipped and counted,
 *     which also bounds the first run after a start: it begins at the oldest slot whose input is still
 *     complete.</li>
 * </ul>
 */
public class OtlpLinkJoinLauncher {

    public static final String JOB_NAME = "otlpLinkJoinJob";
    public static final String SLOT_PARAMETER = "slot";

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final JobLauncher jobLauncher;
    private final Job job;
    private final long settleMillis;
    private final long ttlMillis;
    private final LongSupplier clock;

    private volatile long lastDoneSlot = Long.MIN_VALUE;
    private final AtomicLong skippedSlots = new AtomicLong();
    private final AtomicLong failedLaunches = new AtomicLong();

    public OtlpLinkJoinLauncher(JobLauncher jobLauncher, Job job, long settleMillis, long ttlMillis, LongSupplier clock) {
        this.jobLauncher = Objects.requireNonNull(jobLauncher, "jobLauncher");
        this.job = Objects.requireNonNull(job, "job");
        if (settleMillis < 0 || ttlMillis < 1) {
            throw new IllegalArgumentException("settle must not be negative, ttl must be positive");
        }
        this.settleMillis = settleMillis;
        this.ttlMillis = ttlMillis;
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public static JobParameters jobParameters(long slot) {
        return new JobParametersBuilder()
                .addLong(SLOT_PARAMETER, slot, true)
                .toJobParameters();
    }

    /**
     * Whether the input of the job of {@code slot} is no longer complete. The job reads {@code slot - 1}
     * too, whose keys expire first, at {@code slot start + ttl} (plus the per-bucket spread). After that
     * a join would see a partial input and a retry would overwrite correct absolute values with smaller
     * ones, so the slot is not run any more.
     */
    static boolean expired(long slot, long now, long ttlMillis) {
        return now > LinkIndexKeys.slotStartMillis(slot) + ttlMillis;
    }

    public synchronized void run() {
        final long start = clock.getAsLong();
        // the last slot whose end + settle has passed; fixed for this run, the next trigger takes the rest
        final long target = LinkIndexKeys.slotOf(start - settleMillis) - 1;
        final long from = lastDoneSlot == Long.MIN_VALUE ? oldestNotExpired(start) : lastDoneSlot + 1;

        for (long slot = from; slot <= target; slot++) {
            // re-read the clock per slot: the slots before may have taken minutes, and the input of this
            // one may have expired meanwhile
            final long now = clock.getAsLong();
            if (expired(slot, now, ttlMillis)) {
                skippedSlots.incrementAndGet();
                logger.warn("Skip slot:{}: its input expired {}ms ago (slot start + ttl {}ms)",
                        slot, now - (LinkIndexKeys.slotStartMillis(slot) + ttlMillis), ttlMillis);
                lastDoneSlot = slot;
                continue;
            }
            if (!launch(slot)) {
                return;
            }
            lastDoneSlot = slot;
        }
    }

    /** the oldest slot whose input (itself and the slot before) has not expired */
    private long oldestNotExpired(long now) {
        final long slot = LinkIndexKeys.slotOf(now - ttlMillis);
        return expired(slot, now, ttlMillis) ? slot + 1 : slot;
    }

    /**
     * @return {@code true} if the slot is done (completed now or before)
     */
    private boolean launch(long slot) {
        try {
            final JobExecution execution = jobLauncher.run(job, jobParameters(slot));
            if (execution.getStatus() == BatchStatus.COMPLETED) {
                return true;
            }
            failedLaunches.incrementAndGet();
            logger.warn("{} slot:{} ended with {} {}", JOB_NAME, slot, execution.getStatus(), execution.getExitStatus());
            return false;
        } catch (JobInstanceAlreadyCompleteException e) {
            logger.debug("{} slot:{} already completed", JOB_NAME, slot);
            return true;
        } catch (JobExecutionAlreadyRunningException e) {
            logger.info("{} slot:{} is running elsewhere", JOB_NAME, slot);
            return false;
        } catch (Exception e) {
            failedLaunches.incrementAndGet();
            logger.warn("{} slot:{} launch failed", JOB_NAME, slot, e);
            return false;
        }
    }

    /** slots skipped because their input had expired before the job could run them */
    public long getSkippedSlots() {
        return skippedSlots.get();
    }

    /** launches that ended FAILED or threw (a slot retried every minute counts every attempt) */
    public long getFailedLaunches() {
        return failedLaunches.get();
    }

    public long getLastDoneSlot() {
        return lastDoneSlot;
    }

    /**
     * Seconds since the end of the last slot this launcher completed or skipped; 0 before the first run.
     * In steady state between the settle (30) and settle + one slot (90), depending on when it is sampled
     * relative to the cron; it keeps growing while the job is failing or the batch is stopped.
     */
    public long getLastDoneSlotAgeSeconds() {
        final long done = lastDoneSlot;
        if (done == Long.MIN_VALUE) {
            return 0;
        }
        return Math.max(0, (clock.getAsLong() - LinkIndexKeys.slotEndMillis(done)) / 1000);
    }
}
