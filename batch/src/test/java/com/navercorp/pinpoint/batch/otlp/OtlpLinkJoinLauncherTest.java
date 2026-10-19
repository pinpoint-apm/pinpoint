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
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OtlpLinkJoinLauncherTest {

    private static final long TTL = 300_000;
    private static final long SETTLE = 30_000;

    private final Job job = mock(Job.class);
    private final AtomicLong now = new AtomicLong();
    private final List<Long> launched = new ArrayList<>();
    private final Set<Long> completedBefore = new HashSet<>();
    private final Set<Long> runningElsewhere = new HashSet<>();
    private final Set<Long> failing = new HashSet<>();
    /** slot -> milliseconds the clock advances while its job runs */
    private final java.util.Map<Long, Long> jobDuration = new java.util.HashMap<>();

    private final long minute = LinkIndexKeys.slotOf(System.currentTimeMillis());

    private JobLauncher jobLauncher() throws Exception {
        JobLauncher jobLauncher = mock(JobLauncher.class);
        when(jobLauncher.run(eq(job), any(JobParameters.class))).thenAnswer(invocation -> {
            JobParameters parameters = invocation.getArgument(1);
            long slot = parameters.getLong(OtlpLinkJoinLauncher.SLOT_PARAMETER);
            if (completedBefore.contains(slot)) {
                throw new JobInstanceAlreadyCompleteException("done");
            }
            if (runningElsewhere.contains(slot)) {
                throw new JobExecutionAlreadyRunningException("running");
            }
            launched.add(slot);
            now.addAndGet(jobDuration.getOrDefault(slot, 0L));
            JobExecution execution = new JobExecution(1L, parameters);
            execution.setStatus(failing.contains(slot) ? BatchStatus.FAILED : BatchStatus.COMPLETED);
            return execution;
        });
        return jobLauncher;
    }

    private OtlpLinkJoinLauncher launcher() throws Exception {
        return new OtlpLinkJoinLauncher(jobLauncher(), job, SETTLE, TTL, now::get);
    }

    /** the cron fires at second 30 of minute {@code m}: slot m-1 has ended */
    private void cronAt(long m) {
        now.set(LinkIndexKeys.slotStartMillis(m) + 30_000);
    }

    @Test
    void slotIsTheOnlyIdentifyingParameter() {
        JobParameters parameters = OtlpLinkJoinLauncher.jobParameters(42);
        assertThat(parameters.getParameters()).containsOnlyKeys(OtlpLinkJoinLauncher.SLOT_PARAMETER);
        assertThat(parameters.getParameters().get(OtlpLinkJoinLauncher.SLOT_PARAMETER).isIdentifying()).isTrue();
    }

    @Test
    void theInputOfASlotExpiresWithThePreviousSlot() {
        long slot = 1000;
        // the job of 1000 reads 999 too, whose keys expire at (end of 999 = start of 1000) + ttl
        assertThat(OtlpLinkJoinLauncher.expired(slot, LinkIndexKeys.slotStartMillis(slot) + TTL, TTL)).isFalse();
        assertThat(OtlpLinkJoinLauncher.expired(slot, LinkIndexKeys.slotStartMillis(slot) + TTL + 1, TTL)).isTrue();
    }

    @Test
    void runsTheSlotThatJustEnded() throws Exception {
        OtlpLinkJoinLauncher launcher = launcher();
        assertThat(launcher.getLastDoneSlotAgeSeconds()).isZero();
        cronAt(minute);
        launcher.run();
        assertThat(launched).containsExactly(minute - 4, minute - 3, minute - 2, minute - 1);
        assertThat(launcher.getLastDoneSlotAgeSeconds()).isEqualTo(30);

        launched.clear();
        cronAt(minute + 1);
        launcher.run();
        assertThat(launched).containsExactly(minute);
    }

    @Test
    void firstRunStartsAtTheOldestSlotWhoseInputIsCompleteAndSkipsCompletedSlots() throws Exception {
        OtlpLinkJoinLauncher launcher = launcher();
        completedBefore.add(minute - 4);
        completedBefore.add(minute - 3);
        cronAt(minute);

        launcher.run();

        // at m:00:30 the keys of m-6 (slot end m-5:00 + ttl 5m = m:00) have begun to expire: m-5 is not run,
        // m-4 is the oldest slot whose input (m-5 and m-4) is complete
        assertThat(launched).containsExactly(minute - 2, minute - 1);
        assertThat(launcher.getSkippedSlots()).isZero();
        assertThat(launcher.getLastDoneSlot()).isEqualTo(minute - 1);
    }

    @Test
    void failureStopsAndTheNextRunRetriesTheSameSlot() throws Exception {
        OtlpLinkJoinLauncher launcher = launcher();
        cronAt(minute);
        launcher.run();
        launched.clear();

        failing.add(minute);
        cronAt(minute + 2);
        launcher.run();
        // slot m failed: m+1 is not started because its cleanup would delete the keys slot m still needs
        assertThat(launched).containsExactly(minute);
        assertThat(launcher.getFailedLaunches()).isEqualTo(1);
        // the last done slot is still m-1, which ended 2m30s ago
        assertThat(launcher.getLastDoneSlotAgeSeconds()).isEqualTo(150);

        failing.clear();
        launched.clear();
        cronAt(minute + 3);
        launcher.run();
        assertThat(launched).containsExactly(minute, minute + 1, minute + 2);
        assertThat(launcher.getFailedLaunches()).isEqualTo(1);
        assertThat(launcher.getLastDoneSlotAgeSeconds()).isEqualTo(30);
    }

    @Test
    void failedSlotIsRetriedByEveryRunUntilItsInputExpires() throws Exception {
        OtlpLinkJoinLauncher launcher = launcher();
        cronAt(minute);
        launcher.run();
        launched.clear();

        failing.add(minute);
        for (long m = minute + 1; m <= minute + 4; m++) {
            cronAt(m);
            launcher.run();
        }
        // four runs, four attempts of slot m and nothing else, however far the window has moved
        assertThat(launched).containsExactly(minute, minute, minute, minute);
        assertThat(launcher.getFailedLaunches()).isEqualTo(4);

        launched.clear();
        cronAt(minute + 5);
        launcher.run();
        // m+5:00:30: the input of m (keys of m-1, expiring from m:00 + 5m = m+5:00) has expired, m+1's has not
        assertThat(launcher.getSkippedSlots()).isEqualTo(1);
        assertThat(launched).containsExactly(minute + 1, minute + 2, minute + 3, minute + 4);
        assertThat(launcher.getLastDoneSlot()).isEqualTo(minute + 4);
    }

    @Test
    void aSlotRunningOnAnotherInstanceStopsTheRunWithoutCountingAFailure() throws Exception {
        OtlpLinkJoinLauncher launcher = launcher();
        cronAt(minute);
        launcher.run();
        launched.clear();

        runningElsewhere.add(minute);
        cronAt(minute + 1);
        launcher.run();
        assertThat(launched).isEmpty();
        assertThat(launcher.getFailedLaunches()).isZero();
        assertThat(launcher.getLastDoneSlot()).isEqualTo(minute - 1);

        // completed there in the meantime: this instance moves on
        runningElsewhere.clear();
        completedBefore.add(minute);
        cronAt(minute + 2);
        launcher.run();
        assertThat(launched).containsExactly(minute + 1);
    }

    @Test
    void aSlotWhoseInputExpiredWhileTheSlotsBeforeItRanIsSkipped() throws Exception {
        OtlpLinkJoinLauncher launcher = launcher();
        // the first run catches up m-4 .. m-1; the job of m-4 takes two minutes
        jobDuration.put(minute - 4, 120_000L);
        cronAt(minute);

        launcher.run();

        // at m:02:30 the input of m-3 (keys of m-4, expiring from m-3:00 + 5m = m+2:00) has expired: skipped,
        // not run with a partial input; m-2 (deadline m+3:00) and m-1 still run
        assertThat(launched).containsExactly(minute - 4, minute - 2, minute - 1);
        assertThat(launcher.getSkippedSlots()).isEqualTo(1);
        assertThat(launcher.getLastDoneSlot()).isEqualTo(minute - 1);
    }

    @Test
    void afterAPauseTheRetryPositionIsKeptAndTheExpiredSlotsAreSkipped() throws Exception {
        OtlpLinkJoinLauncher launcher = launcher();
        cronAt(minute);
        launcher.run();
        launched.clear();

        cronAt(minute + 10);
        launcher.run();
        // m .. m+5 expired (input of m+5 = keys of m+4, expiring from m+5:00 + 5m = m+10:00)
        assertThat(launcher.getSkippedSlots()).isEqualTo(6);
        assertThat(launched).containsExactly(minute + 6, minute + 7, minute + 8, minute + 9);
    }

    @Test
    void parametersCarryTheSlot() throws Exception {
        JobLauncher jobLauncher = jobLauncher();
        OtlpLinkJoinLauncher launcher = new OtlpLinkJoinLauncher(jobLauncher, job, SETTLE, TTL, now::get);
        completedBefore.add(minute - 4);
        completedBefore.add(minute - 3);
        completedBefore.add(minute - 2);
        cronAt(minute);
        launcher.run();

        ArgumentCaptor<JobParameters> captor = ArgumentCaptor.forClass(JobParameters.class);
        org.mockito.Mockito.verify(jobLauncher, org.mockito.Mockito.times(4)).run(eq(job), captor.capture());
        assertThat(captor.getAllValues().get(3).getLong(OtlpLinkJoinLauncher.SLOT_PARAMETER)).isEqualTo(minute - 1);
    }
}
