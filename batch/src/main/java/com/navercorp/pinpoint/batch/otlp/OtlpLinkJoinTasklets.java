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

import com.navercorp.pinpoint.otlp.trace.linkindex.LinkJoinService;
import com.navercorp.pinpoint.otlp.trace.linkindex.suppress.LinkCounts;
import com.navercorp.pinpoint.otlp.trace.linkindex.suppress.SuppressManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.Map;
import java.util.Objects;
import java.util.function.LongSupplier;

/**
 * The two steps of {@code otlpLinkJoinJob}.
 * The join step deletes nothing, so a failed join is re-run from the start with the same result;
 * once it has completed, a restart only runs the cleanup step.
 */
public final class OtlpLinkJoinTasklets {

    private OtlpLinkJoinTasklets() {
    }

    static long slot(ChunkContext chunkContext) {
        final Map<String, Object> parameters = chunkContext.getStepContext().getJobParameters();
        final Object slot = parameters.get(OtlpLinkJoinLauncher.SLOT_PARAMETER);
        if (slot instanceof Number number) {
            return number.longValue();
        }
        throw new IllegalArgumentException("Missing job parameter " + OtlpLinkJoinLauncher.SLOT_PARAMETER);
    }

    public static class Join implements Tasklet {
        private final LinkJoinService service;

        public Join(LinkJoinService service) {
            this.service = Objects.requireNonNull(service, "service");
        }

        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
            final LinkJoinService.JoinSummary summary = service.join(slot(chunkContext));
            contribution.incrementReadCount();
            contribution.incrementWriteCount(summary.rows());
            return RepeatStatus.FINISHED;
        }
    }

    /**
     * Updates the suppression list from the outcome of the join step of this run. Runs after the cleanup step, whose
     * "previous slot completed" check thereby orders the list updates of consecutive slots, also across batch
     * instances: the list is read, recomputed and written without a lock. The counts live in the
     * {@link LinkJoinService} of this instance: if the join step of this slot completed in an earlier run (the job
     * was restarted after a later step failed), there is nothing to apply and the step completes without a change.
     * {@link SuppressManager#apply} never throws, so a store failure never fails the job: the next slot recomputes
     * the same transitions.
     */
    public static class Suppress implements Tasklet {
        private final Logger logger = LogManager.getLogger(this.getClass());

        private final LinkJoinService service;
        private final SuppressManager manager;

        public Suppress(LinkJoinService service, SuppressManager manager) {
            this.service = Objects.requireNonNull(service, "service");
            this.manager = Objects.requireNonNull(manager, "manager");
        }

        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
            final long slot = slot(chunkContext);
            final Map<String, LinkCounts> counts = service.getClientCounts(slot);
            if (counts == null) {
                logger.warn("No join outcome of slot {} in this instance: suppression list not updated", slot);
                return RepeatStatus.FINISHED;
            }
            final SuppressManager.SuppressSummary summary = manager.apply(slot, counts);
            contribution.incrementReadCount();
            contribution.incrementWriteCount(summary.activated() + summary.released() + summary.expired());
            return RepeatStatus.FINISHED;
        }
    }

    /**
     * Deletes the keys of the previous slot, which the job of that slot reads. The launcher of one instance
     * never runs slot k before k-1 has completed; this step enforces the same order against the jobs of
     * other instances through the JobRepository: the keys go when the job of k-1 has completed, or when
     * nobody can run it any more because its own input has expired. Otherwise the step fails and the
     * next run retries it (the join step of k has completed and is not run again).
     */
    public static class Cleanup implements Tasklet {
        private final Logger logger = LogManager.getLogger(this.getClass());

        private final LinkJoinService service;
        private final JobRepository jobRepository;
        private final long ttlMillis;
        private final LongSupplier clock;

        public Cleanup(LinkJoinService service, JobRepository jobRepository, long ttlMillis, LongSupplier clock) {
            this.service = Objects.requireNonNull(service, "service");
            this.jobRepository = Objects.requireNonNull(jobRepository, "jobRepository");
            if (ttlMillis < 1) {
                throw new IllegalArgumentException("ttl must be positive");
            }
            this.ttlMillis = ttlMillis;
            this.clock = Objects.requireNonNull(clock, "clock");
        }

        @Override
        public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
            final long slot = slot(chunkContext);
            if (!previousSlotDone(slot - 1)) {
                throw new IllegalStateException(OtlpLinkJoinLauncher.JOB_NAME + " slot:" + (slot - 1)
                        + " has not completed: the cleanup of slot " + slot + " would delete its input, retried by the next run");
            }
            service.cleanup(slot);
            return RepeatStatus.FINISHED;
        }

        /**
         * @return {@code true} if the job of {@code previous} has completed, or can no longer be run because
         * its input has expired ({@link OtlpLinkJoinLauncher#expired}); a STARTED execution left by a crashed
         * instance is released the same way once the input has expired
         */
        boolean previousSlotDone(long previous) {
            final JobExecution last = jobRepository.getLastJobExecution(OtlpLinkJoinLauncher.JOB_NAME, OtlpLinkJoinLauncher.jobParameters(previous));
            if (last != null && last.getStatus() == BatchStatus.COMPLETED) {
                return true;
            }
            final String state = last == null ? "never run" : String.valueOf(last.getStatus());
            if (OtlpLinkJoinLauncher.expired(previous, clock.getAsLong(), ttlMillis)) {
                if (last != null && last.isRunning()) {
                    logger.warn("{} slot:{} is {} but its input has expired: deleting its keys", OtlpLinkJoinLauncher.JOB_NAME, previous, state);
                }
                return true;
            }
            logger.info("{} slot:{} is {} and its input has not expired: keeping its keys", OtlpLinkJoinLauncher.JOB_NAME, previous, state);
            return false;
        }
    }
}
