package com.navercorp.pinpoint.collector.sampler;

import com.navercorp.pinpoint.collector.config.CollectorProperties;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.common.profiler.util.TransactionIdUtils;
import com.navercorp.pinpoint.common.server.bo.BasicSpan;
import com.navercorp.pinpoint.common.server.bo.SpanBo;
import com.navercorp.pinpoint.common.server.bo.SpanChunkBo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SimpleSpanFactoryTest {

    private CollectorProperties mockProperties;

    @BeforeEach
    public void defaultTestProperties() {
        CollectorProperties mockProperties = mock(CollectorProperties.class);
        when(mockProperties.isSpanSamplingEnable()).thenReturn(true);
        when(mockProperties.getSpanSamplingType()).thenReturn(SamplerType.MOD.name());
        when(mockProperties.getSpanSamplingRate()).thenReturn(5L);
        when(mockProperties.getSpanSamplingPercent()).thenReturn("20");

        this.mockProperties = mockProperties;
    }

    private TransactionId createTransactionId(String agentId, long agentStartTime, long sequenceId) {
        return TransactionIdUtils.parseTransactionId(TransactionIdUtils.formatString(agentId, agentStartTime, sequenceId));
    }

    @Test
    public void TransactionIdSampleTest() {
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

            Assertions.assertEquals(sampler.isSampling(mockSpanBo), sampler.isSampling(mockSpanChunkBo));
        }

        verify(mockSpanBo, atLeastOnce()).getTransactionId();
        verify(mockSpanChunkBo, atLeastOnce()).getTransactionId();
    }

    @Test
    public void disableTest() {
        when(mockProperties.isSpanSamplingEnable()).thenReturn(false);
        SpanSamplerFactory spanSamplerFactory = new SimpleSpanSamplerFactory(mockProperties);

        Sampler<?> sampler = spanSamplerFactory.createBasicSpanSampler();
        Assertions.assertTrue(sampler instanceof TrueSampler);
    }

    @Test
    public void modSamplerTest() {
        SpanSamplerFactory spanSamplerFactory = new SimpleSpanSamplerFactory(mockProperties);

        Sampler<?> sampler = spanSamplerFactory.createBasicSpanSampler();
        Assertions.assertTrue(sampler instanceof ModSampler<?>);
    }

    @Test
    public void percentageSamplerTest() {
        when(mockProperties.getSpanSamplingType()).thenReturn(SamplerType.PERCENT.name());
        SpanSamplerFactory spanSamplerFactory = new SimpleSpanSamplerFactory(mockProperties);

        Sampler<?> sampler = spanSamplerFactory.createBasicSpanSampler();
        Assertions.assertTrue(sampler instanceof PercentRateSampler<?>);
    }

    @Test
    public void trueSamplerTest1() {
        when(mockProperties.getSpanSamplingRate()).thenReturn(1L);
        SpanSamplerFactory spanSamplerFactory = new SimpleSpanSamplerFactory(mockProperties);

        Sampler<?> sampler = spanSamplerFactory.createBasicSpanSampler();
        Assertions.assertTrue(sampler instanceof TrueSampler);
    }

    @Test
    public void trueSamplerTest2() {
        when(mockProperties.getSpanSamplingType()).thenReturn(SamplerType.PERCENT.name());
        when(mockProperties.getSpanSamplingPercent()).thenReturn("100");
        SpanSamplerFactory spanSamplerFactory = new SimpleSpanSamplerFactory(mockProperties);

        Sampler<?> sampler = spanSamplerFactory.createBasicSpanSampler();
        Assertions.assertTrue(sampler instanceof TrueSampler);
    }

    @Test
    public void falseSamplerTest1() {
        when(mockProperties.getSpanSamplingType()).thenReturn(SamplerType.PERCENT.name());
        when(mockProperties.getSpanSamplingPercent()).thenReturn("0");
        SpanSamplerFactory spanSamplerFactory = new SimpleSpanSamplerFactory(mockProperties);

        Sampler<?> sampler = spanSamplerFactory.createBasicSpanSampler();
        Assertions.assertTrue(sampler instanceof FalseSampler);
    }

}
