/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.sampler;

import com.navercorp.pinpoint.collector.config.CollectorProperties;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.server.bo.BasicSpan;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SimpleSpanFactoryTest {

    private CollectorProperties mockProperties;

    @BeforeEach
    public void defaultSamplingProperties() {
        CollectorProperties mockProperties = mock(CollectorProperties.class);
        when(mockProperties.isSpanSamplingEnable()).thenReturn(false);
        when(mockProperties.getSpanSamplingType()).thenReturn(SamplerType.MOD.name());
        when(mockProperties.getSpanModSamplingRate()).thenReturn(1L);
        when(mockProperties.getSpanPercentSamplingRate()).thenReturn("0");

        this.mockProperties = mockProperties;
    }

    @Test
    public void TransactionIdSampleTest() {
        when(mockProperties.isSpanSamplingEnable()).thenReturn(true);
        when(mockProperties.getSpanSamplingType()).thenReturn(SamplerType.MOD.name());
        when(mockProperties.getSpanModSamplingRate()).thenReturn(5L);
        SpanSamplerFactory spanSamplerFactory = new SimpleSpanSamplerFactory(mockProperties);
        Sampler<BasicSpan> sampler = spanSamplerFactory.createBasicSpanSampler();

        String agentId = "testAgentId";
        long time = System.currentTimeMillis();
        SpanChunkBo mockSpanChunkBo = mock(SpanChunkBo.class);
        SpanBo mockSpanBo = mock(SpanBo.class);
        for (long i = 0; i < 10; i++) {
            TransactionId transactionId = createTransactionId(agentId, time, i);
            when(mockSpanBo.getTransactionId()).thenReturn(transactionId);
            when(mockSpanChunkBo.getTransactionId()).thenReturn(transactionId);

            assertThat(sampler.isSampling(mockSpanBo)).isEqualTo(sampler.isSampling(mockSpanChunkBo));
        }

        verify(mockSpanBo, atLeastOnce()).getTransactionId();
        verify(mockSpanChunkBo, atLeastOnce()).getTransactionId();
    }

    private TransactionId createTransactionId(String agentId, long agentStartTime, long sequenceId) {
        return TransactionId.of(agentId, agentStartTime, sequenceId);
    }

    @Test
    public void disableTest() {
        when(mockProperties.isSpanSamplingEnable()).thenReturn(false);
        SpanSamplerFactory spanSamplerFactory = new SimpleSpanSamplerFactory(mockProperties);
        Sampler<?> sampler = spanSamplerFactory.createBasicSpanSampler();

        assertThat(sampler).isInstanceOf(TrueSampler.class);
    }

    @Test
    public void modSamplerTest() {
        when(mockProperties.isSpanSamplingEnable()).thenReturn(true);
        when(mockProperties.getSpanSamplingType()).thenReturn(SamplerType.MOD.name());
        when(mockProperties.getSpanModSamplingRate()).thenReturn(5L);
        SpanSamplerFactory spanSamplerFactory = new SimpleSpanSamplerFactory(mockProperties);
        Sampler<?> sampler = spanSamplerFactory.createBasicSpanSampler();

        assertThat(sampler).isInstanceOf(ModSampler.class);
    }

    @Test
    public void percentageSamplerTest() {
        when(mockProperties.isSpanSamplingEnable()).thenReturn(true);
        when(mockProperties.getSpanSamplingType()).thenReturn(SamplerType.PERCENT.name());
        when(mockProperties.getSpanPercentSamplingRate()).thenReturn("20");
        SpanSamplerFactory spanSamplerFactory = new SimpleSpanSamplerFactory(mockProperties);
        Sampler<?> sampler = spanSamplerFactory.createBasicSpanSampler();

        assertThat(sampler).isInstanceOf(PercentRateSampler.class);
    }

    @Test
    public void trueSamplerTest1() {
        when(mockProperties.isSpanSamplingEnable()).thenReturn(true);
        when(mockProperties.getSpanSamplingType()).thenReturn(SamplerType.MOD.name());
        when(mockProperties.getSpanModSamplingRate()).thenReturn(1L);
        SpanSamplerFactory spanSamplerFactory = new SimpleSpanSamplerFactory(mockProperties);
        Sampler<?> sampler = spanSamplerFactory.createBasicSpanSampler();

        assertThat(sampler).isInstanceOf(TrueSampler.class);
    }

    @Test
    public void trueSamplerTest2() {
        when(mockProperties.isSpanSamplingEnable()).thenReturn(true);
        when(mockProperties.getSpanSamplingType()).thenReturn(SamplerType.PERCENT.name());
        when(mockProperties.getSpanPercentSamplingRate()).thenReturn("100");
        SpanSamplerFactory spanSamplerFactory = new SimpleSpanSamplerFactory(mockProperties);
        Sampler<?> sampler = spanSamplerFactory.createBasicSpanSampler();

        assertThat(sampler).isInstanceOf(TrueSampler.class);
    }

    @Test
    public void falseSamplerTest1() {
        when(mockProperties.isSpanSamplingEnable()).thenReturn(true);
        when(mockProperties.getSpanSamplingType()).thenReturn(SamplerType.PERCENT.name());
        when(mockProperties.getSpanPercentSamplingRate()).thenReturn("0");
        SpanSamplerFactory spanSamplerFactory = new SimpleSpanSamplerFactory(mockProperties);
        Sampler<?> sampler = spanSamplerFactory.createBasicSpanSampler();

        assertThat(sampler).isInstanceOf(FalseSampler.class);
    }
}
