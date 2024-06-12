package com.navercorp.pinpoint.collector.sampler;

import com.navercorp.pinpoint.collector.config.CollectorProperties;
import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
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

        AgentId agentId = AgentId.of("testAgentId");
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

    private TransactionId createTransactionId(AgentId agentId, long agentStartTime, long sequenceId) {
        return TransactionIdUtils.parseTransactionId(TransactionIdUtils.formatString(agentId, agentStartTime, sequenceId));
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
