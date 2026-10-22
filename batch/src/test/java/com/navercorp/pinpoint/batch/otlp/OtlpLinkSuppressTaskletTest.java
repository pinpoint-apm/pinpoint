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
import com.navercorp.pinpoint.otlp.trace.linkindex.suppress.SuppressKey;
import com.navercorp.pinpoint.otlp.trace.linkindex.suppress.SuppressManager;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OtlpLinkSuppressTaskletTest {
    private static final long SLOT = 1000;

    private final LinkJoinService service = mock(LinkJoinService.class);
    private final SuppressManager manager = mock(SuppressManager.class);
    private final OtlpLinkJoinTasklets.Suppress tasklet = new OtlpLinkJoinTasklets.Suppress(service, manager);

    private static ChunkContext chunkContext(long slot) {
        JobExecution jobExecution = new JobExecution(1L, OtlpLinkJoinLauncher.jobParameters(slot));
        StepExecution stepExecution = new StepExecution("otlpLinkSuppressStep", jobExecution);
        return new ChunkContext(new StepContext(stepExecution));
    }

    @Test
    void appliesTheJoinOutcomeOfTheSlot() {
        Map<String, LinkCounts> counts = Map.of(SuppressKey.of("mapmatch-bg", "navi:12000"), new LinkCounts(0, 7_000));
        when(service.getClientCounts(SLOT)).thenReturn(counts);
        when(manager.apply(SLOT, counts)).thenReturn(new SuppressManager.SuppressSummary(SLOT, 1, 0, 1, 0, 2, false));
        StepContribution contribution = mock(StepContribution.class);

        assertThat(tasklet.execute(contribution, chunkContext(SLOT))).isEqualTo(RepeatStatus.FINISHED);

        verify(manager).apply(SLOT, counts);
        verify(contribution).incrementWriteCount(3);
    }

    @Test
    void completesWithoutAChangeWhenThisInstanceDidNotJoinTheSlot() {
        when(service.getClientCounts(SLOT)).thenReturn(null);

        assertThat(tasklet.execute(mock(StepContribution.class), chunkContext(SLOT))).isEqualTo(RepeatStatus.FINISHED);

        verify(manager, never()).apply(anyLong(), anyMap());
    }
}
