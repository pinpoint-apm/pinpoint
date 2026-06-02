/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.collector.sampling.tail;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TailSamplingSweeperTest {

    @Test
    void sweep_forcesKeepDecisionForStaleTxids_andReplays() {
        TailSamplingRepository repository = Mockito.mock(TailSamplingRepository.class);
        TailSampler tailSampler = Mockito.mock(TailSampler.class);
        TailSamplingProperties props = new TailSamplingProperties();
        props.setBufferTtl(Duration.ofSeconds(300));

        when(repository.findStale(anyLong(), anyInt())).thenReturn(List.of("tx-stale"));
        List<byte[]> spans = List.of(new byte[]{1});
        when(repository.decide(eq("tx-stale"), eq(true))).thenReturn(spans);

        TailSamplingSweeper sweeper = new TailSamplingSweeper(repository, tailSampler, props);
        sweeper.sweep();

        verify(repository).decide("tx-stale", true); // default keep
        verify(tailSampler).replaySwept(spans);
    }

    @Test
    void sweep_skipsWhenDecideReturnsNull() {
        TailSamplingRepository repository = Mockito.mock(TailSamplingRepository.class);
        TailSampler tailSampler = Mockito.mock(TailSampler.class);
        TailSamplingProperties props = new TailSamplingProperties();

        when(repository.findStale(anyLong(), anyInt())).thenReturn(List.of("tx-x"));
        when(repository.decide(eq("tx-x"), eq(true))).thenReturn(null); // another node won

        TailSamplingSweeper sweeper = new TailSamplingSweeper(repository, tailSampler, props);
        sweeper.sweep();

        verify(tailSampler, Mockito.never()).replaySwept(Mockito.any());
    }

    @Test
    void finalizeDeferred_finalizesDueTraces() {
        TailSamplingRepository repository = Mockito.mock(TailSamplingRepository.class);
        TailSampler tailSampler = Mockito.mock(TailSampler.class);
        TailSamplingProperties props = new TailSamplingProperties();
        props.setDecisionGrace(Duration.ofSeconds(2));

        when(repository.findDeferredDue(anyLong(), anyInt())).thenReturn(List.of("tx-deferred"));

        TailSamplingSweeper sweeper = new TailSamplingSweeper(repository, tailSampler, props);
        sweeper.finalizeDeferred();

        verify(tailSampler).finalizeDeferred("tx-deferred");
    }
}
