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

import com.navercorp.pinpoint.collector.service.TraceService;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import com.navercorp.pinpoint.common.server.io.GrpcSpanFactory;
import com.navercorp.pinpoint.common.server.trace.PinpointServerTraceId;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TailSamplerTest {

    private StatisticsTraceService always;
    private TraceService sampled;
    private TailSamplingRepository repository;
    private GrpcSpanFactory factory;
    private TailSampler tailSampler;

    @BeforeEach
    void setUp() {
        always = Mockito.mock(StatisticsTraceService.class);
        sampled = Mockito.mock(TraceService.class);
        repository = Mockito.mock(TailSamplingRepository.class);
        factory = Mockito.mock(GrpcSpanFactory.class);
        TailSamplingProperties props = new TailSamplingProperties();
        props.setBands(List.of()); // rateFor -> 100

        tailSampler = new TailSampler(
                new TraceService[]{always, sampled},
                repository, props, new BufferedSpanCodec(), factory, new SimpleMeterRegistry());
    }

    private SpanBo rootSpan(int elapsed) {
        SpanBo bo = new SpanBo();
        bo.setTransactionId(new PinpointServerTraceId("agent", 1L, 100L));
        bo.setParentSpanId(-1L); // root
        bo.setElapsed(elapsed);
        bo.setAgentId("agent");
        bo.setApplicationName("app");
        bo.setAgentName("agentName");
        return bo;
    }

    @Test
    void alwaysGroupCalledImmediately_regardlessOfDecision() {
        when(repository.accept(anyString(), any(), anyLong(), anyBoolean())).thenReturn("buffered");
        SpanBo bo = rootSpan(10);

        tailSampler.acceptSpan(bo, new byte[]{1});

        verify(always).insertSpan(bo);
    }

    @Test
    void decisionDrop_sampledNotWritten() {
        when(repository.accept(anyString(), any(), anyLong(), anyBoolean())).thenReturn("drop");
        SpanBo bo = rootSpan(10);

        tailSampler.acceptSpan(bo, new byte[]{1});

        verify(sampled, never()).insertSpan(any());
        verify(always).insertSpan(bo);
    }

    @Test
    void decisionKeep_writesThroughToSampledLive() {
        when(repository.accept(anyString(), any(), anyLong(), anyBoolean())).thenReturn("keep");
        SpanBo bo = rootSpan(10);

        tailSampler.acceptSpan(bo, new byte[]{1});

        verify(sampled).insertSpan(bo);
    }

    @Test
    void rootBuffered_triggersDecide_andReplaysKeptSpans() {
        when(repository.accept(anyString(), any(), anyLong(), anyBoolean())).thenReturn("buffered");
        BufferedSpan buffered = new BufferedSpan(BufferedSpan.Type.SPAN,
                "agent", "agentName", "app", 1L, 2L,
                com.navercorp.pinpoint.grpc.trace.PSpan.newBuilder().build().toByteArray());
        byte[] encoded = new BufferedSpanCodec().encode(buffered);
        when(repository.decide(anyString(), org.mockito.ArgumentMatchers.eq(true)))
                .thenReturn(List.of(encoded));
        SpanBo rebuilt = new SpanBo();
        when(factory.buildSpanBo(any(), any(), anyLong())).thenReturn(rebuilt);

        tailSampler.acceptSpan(rootSpan(10), new byte[]{1});

        verify(sampled).insertSpan(rebuilt);
    }

    @Test
    void redisFailure_failsOpen_writesSampledLive() {
        when(repository.accept(anyString(), any(), anyLong(), anyBoolean()))
                .thenThrow(new RuntimeException("redis down"));
        SpanBo bo = rootSpan(10);

        tailSampler.acceptSpan(bo, new byte[]{1});

        verify(always).insertSpan(bo);
        verify(sampled).insertSpan(bo); // fail-open
    }

    private SpanChunkBo chunk() {
        SpanChunkBo bo = new SpanChunkBo();
        bo.setTransactionId(new PinpointServerTraceId("agent", 1L, 200L));
        bo.setAgentId("agent");
        bo.setApplicationName("app");
        bo.setAgentStartTime(1L);
        return bo;
    }

    @Test
    void chunk_alwaysGroupCalledImmediately() {
        when(repository.accept(anyString(), any(), anyLong(), anyBoolean())).thenReturn("buffered");
        SpanChunkBo bo = chunk();
        tailSampler.acceptSpanChunk(bo, new byte[]{1});
        verify(always).insertSpanChunk(bo);
    }

    @Test
    void chunk_keep_writesThroughToSampledLive() {
        when(repository.accept(anyString(), any(), anyLong(), anyBoolean())).thenReturn("keep");
        SpanChunkBo bo = chunk();
        tailSampler.acceptSpanChunk(bo, new byte[]{1});
        verify(sampled).insertSpanChunk(bo);
    }

    @Test
    void chunk_drop_notWritten() {
        when(repository.accept(anyString(), any(), anyLong(), anyBoolean())).thenReturn("drop");
        SpanChunkBo bo = chunk();
        tailSampler.acceptSpanChunk(bo, new byte[]{1});
        verify(sampled, never()).insertSpanChunk(any());
    }

    @Test
    void chunk_redisFailure_failsOpen() {
        when(repository.accept(anyString(), any(), anyLong(), anyBoolean())).thenThrow(new RuntimeException("down"));
        SpanChunkBo bo = chunk();
        tailSampler.acceptSpanChunk(bo, new byte[]{1});
        verify(sampled).insertSpanChunk(bo);
    }

    /** Builds a sampler whose band drops everything (rate 0), with the given keep-on-error toggle. */
    private TailSampler dropAllSampler(boolean keepOnError) {
        TailSamplingProperties props = new TailSamplingProperties();
        TailSamplingProperties.Band dropAll = new TailSamplingProperties.Band();
        dropAll.setRate(0); // catch-all, drop everything by response time
        props.setBands(List.of(dropAll));
        props.setKeepOnError(keepOnError);
        return new TailSampler(new TraceService[]{always, sampled},
                repository, props, new BufferedSpanCodec(), factory, new SimpleMeterRegistry());
    }

    @Test
    void erroredSpan_setsErrorFlag_andReplaysWhenDecideUpgradesToKeep() {
        when(repository.accept(anyString(), any(), anyLong(), anyBoolean())).thenReturn("buffered");
        // band rate 0 -> proposed decision is drop (false); simulate the script upgrading to keep
        // (error flag) by returning buffered spans for the proposed=drop call.
        BufferedSpan buffered = new BufferedSpan(BufferedSpan.Type.SPAN, "agent", "agentName", "app", 1L, 2L,
                com.navercorp.pinpoint.grpc.trace.PSpan.newBuilder().build().toByteArray());
        when(repository.decide(anyString(), eq(false)))
                .thenReturn(List.of(new BufferedSpanCodec().encode(buffered)));
        when(factory.buildSpanBo(any(), any(), anyLong())).thenReturn(new SpanBo());

        TailSampler sampler = dropAllSampler(true);
        SpanBo bo = rootSpan(10); // < any band; band would drop by response time
        bo.setErrCode(1);         // but it is an error

        sampler.acceptSpan(bo, com.navercorp.pinpoint.grpc.trace.PSpan.newBuilder().build().toByteArray());

        verify(repository).accept(anyString(), any(), anyLong(), eq(true)); // error flag propagated
        verify(sampled).insertSpan(any());                                   // kept via upgrade (non-empty result)
    }

    @Test
    void keepOnErrorDisabled_doesNotSetErrorFlag_andDrops() {
        when(repository.accept(anyString(), any(), anyLong(), anyBoolean())).thenReturn("buffered");
        when(repository.decide(anyString(), eq(false))).thenReturn(List.of()); // drop

        TailSampler sampler = dropAllSampler(false);
        SpanBo bo = rootSpan(10);
        bo.setErrCode(1);

        sampler.acceptSpan(bo, com.navercorp.pinpoint.grpc.trace.PSpan.newBuilder().build().toByteArray());

        verify(repository).accept(anyString(), any(), anyLong(), eq(false)); // flag NOT set (toggle off)
        verify(sampled, never()).insertSpan(any());
    }

    @Test
    void erroredChunk_setsErrorFlagOnAccept() {
        when(repository.accept(anyString(), any(), anyLong(), anyBoolean())).thenReturn("buffered");
        com.navercorp.pinpoint.common.server.bo.SpanEventBo event =
                Mockito.mock(com.navercorp.pinpoint.common.server.bo.SpanEventBo.class);
        when(event.hasException()).thenReturn(true);
        SpanChunkBo bo = chunk();
        bo.addSpanEventBoList(List.of(event));

        tailSampler.acceptSpanChunk(bo, new byte[]{1});

        verify(repository).accept(anyString(), any(), anyLong(), eq(true));
    }
}
