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
import com.navercorp.pinpoint.otlp.trace.linkindex.LinkJoinService;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OtlpLinkJoinTaskletsTest {

    private static final long TTL = 300_000;
    private static final long SLOT = 1000;

    private final LinkJoinService service = mock(LinkJoinService.class);
    private final JobRepository jobRepository = mock(JobRepository.class);
    private final AtomicLong now = new AtomicLong(LinkIndexKeys.slotEndMillis(SLOT) + 30_000);
    private final OtlpLinkJoinTasklets.Cleanup cleanup = new OtlpLinkJoinTasklets.Cleanup(service, jobRepository, TTL, now::get);

    private static ChunkContext chunkContext(long slot) {
        JobExecution jobExecution = new JobExecution(1L, OtlpLinkJoinLauncher.jobParameters(slot));
        StepExecution stepExecution = new StepExecution("otlpLinkCleanupStep", jobExecution);
        return new ChunkContext(new StepContext(stepExecution));
    }

    private void previousSlotEndedWith(BatchStatus status) {
        JobExecution execution = new JobExecution(2L, OtlpLinkJoinLauncher.jobParameters(SLOT - 1));
        execution.setStatus(status);
        when(jobRepository.getLastJobExecution(eq(OtlpLinkJoinLauncher.JOB_NAME), eq(OtlpLinkJoinLauncher.jobParameters(SLOT - 1))))
                .thenReturn(execution);
    }

    private void previousInputExpired() {
        now.set(LinkIndexKeys.slotStartMillis(SLOT - 1) + TTL + 1);
    }

    @Test
    void slotComesFromTheJobParameters() {
        assertThat(OtlpLinkJoinTasklets.slot(chunkContext(SLOT))).isEqualTo(SLOT);
    }

    @Test
    void cleansUpWhenThePreviousSlotHasCompleted() {
        previousSlotEndedWith(BatchStatus.COMPLETED);

        assertThat(cleanup.execute(mock(StepContribution.class), chunkContext(SLOT))).isEqualTo(RepeatStatus.FINISHED);
        verify(service).cleanup(SLOT);
    }

    @Test
    void keepsTheKeysWhileThePreviousSlotIsRunningElsewhere() {
        previousSlotEndedWith(BatchStatus.STARTED);

        assertThatThrownBy(() -> cleanup.execute(mock(StepContribution.class), chunkContext(SLOT)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("slot:" + (SLOT - 1));
        verify(service, never()).cleanup(any(Long.class));
    }

    @Test
    void keepsTheKeysWhileTheFailedPreviousSlotCanStillBeRetried() {
        previousSlotEndedWith(BatchStatus.FAILED);

        assertThatThrownBy(() -> cleanup.execute(mock(StepContribution.class), chunkContext(SLOT)))
                .isInstanceOf(IllegalStateException.class);
        verify(service, never()).cleanup(any(Long.class));
    }

    @Test
    void keepsTheKeysWhileThePreviousSlotWasNeverRunAndItsInputIsComplete() {
        assertThatThrownBy(() -> cleanup.execute(mock(StepContribution.class), chunkContext(SLOT)))
                .isInstanceOf(IllegalStateException.class);
        verify(service, never()).cleanup(any(Long.class));
    }

    @Test
    void cleansUpOnceTheInputOfTheFailedPreviousSlotHasExpired() {
        previousSlotEndedWith(BatchStatus.FAILED);
        previousInputExpired();

        cleanup.execute(mock(StepContribution.class), chunkContext(SLOT));
        verify(service).cleanup(SLOT);
    }

    @Test
    void cleansUpOnceTheInputOfANeverRunPreviousSlotHasExpired() {
        previousInputExpired();

        cleanup.execute(mock(StepContribution.class), chunkContext(SLOT));
        verify(service).cleanup(SLOT);
    }

    @Test
    void releasesAStartedExecutionOfACrashedInstanceOnceItsInputHasExpired() {
        previousSlotEndedWith(BatchStatus.STARTED);
        previousInputExpired();

        cleanup.execute(mock(StepContribution.class), chunkContext(SLOT));
        verify(service).cleanup(SLOT);
    }
}
