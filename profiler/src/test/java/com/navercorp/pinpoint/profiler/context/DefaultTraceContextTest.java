/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.util.TransactionId;
import com.navercorp.pinpoint.common.util.TransactionIdUtils;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceId;
import com.navercorp.pinpoint.profiler.context.id.DefaultTransactionCounter;
import com.navercorp.pinpoint.profiler.context.id.TransactionCounter;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
public class DefaultTraceContextTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    @Test
    public void parseTest() {
        String agent= "test";
        long agentStartTime = System.currentTimeMillis();
        long agentTransactionCount = 10;
        TraceId traceId = new DefaultTraceId(agent, agentStartTime, agentTransactionCount);

        String id = traceId.getTransactionId();
        logger.debug("id={}", id);

        TransactionId transactionid = TransactionIdUtils.parseTransactionId(id);

        Assert.assertEquals(transactionid.getAgentId(), agent);
        Assert.assertEquals(transactionid.getAgentStartTime(), agentStartTime);
        Assert.assertEquals(transactionid.getTransactionSequence(), agentTransactionCount);
    }

    @Test
    public void disableTrace() {
        ProfilerConfig profilerConfig = new DefaultProfilerConfig();
        TraceContext traceContext = MockTraceContextFactory.newTestTraceContext(profilerConfig);
        Trace trace = traceContext.disableSampling();
        Assert.assertNotNull(trace);
        Assert.assertFalse(trace.canSampled());

        traceContext.removeTraceObject();
    }

    @Test
    public void threadLocalBindTest() {
        ProfilerConfig profilerConfig = new DefaultProfilerConfig();

        TraceContext traceContext = MockTraceContextFactory.newTestTraceContext(profilerConfig);
        Assert.assertNotNull(traceContext.newTraceObject());

        TraceContext traceContext2 = MockTraceContextFactory.newTestTraceContext(profilerConfig);
        Trace notExist = traceContext2.currentRawTraceObject();
        Assert.assertNull(notExist);

        Assert.assertNotNull(traceContext.currentRawTraceObject());
        traceContext.removeTraceObject();
        Assert.assertNull(traceContext.currentRawTraceObject());
    }
    
    @Test
    public void transactionCountTest() {
        final int samplingRate = 5;

        final ProfilerConfig profilerConfig = Mockito.mock(ProfilerConfig.class);
        Mockito.when(profilerConfig.isTraceAgentActiveThread()).thenReturn(true);
        Mockito.when((profilerConfig.getSamplingRate())).thenReturn(samplingRate);
        Mockito.when((profilerConfig.isSamplingEnable())).thenReturn(true);

        MockTraceContextFactory mockTraceContextFactory = MockTraceContextFactory.newTestTraceContextFactory(profilerConfig);
        final TraceContext traceContext = mockTraceContextFactory.getTraceContext();
        final TransactionCounter transactionCounter = new DefaultTransactionCounter(mockTraceContextFactory.getIdGenerator());


        final long newTransactionCount = 22L;
        @SuppressWarnings("unused")
        final long expectedSampledNewCount = newTransactionCount / samplingRate + (newTransactionCount % samplingRate > 0 ? 1 : 0);
        final long expectedUnsampledNewCount = newTransactionCount - expectedSampledNewCount;
        for (int i = 0; i < newTransactionCount; ++i) {
            traceContext.newTraceObject();
            traceContext.removeTraceObject();
        }
        
        final long expectedSampledContinuationCount = 5L;
        for (int i = 0; i < expectedSampledContinuationCount; ++i) {
            traceContext.continueTraceObject(new DefaultTraceId("agentId", 0L, i));
            traceContext.removeTraceObject();
        }
        
        final long expectedUnsampledContinuationCount = 10L;
        for (int i = 0; i < expectedUnsampledContinuationCount; ++i) {
            traceContext.disableSampling();
            traceContext.removeTraceObject();
        }
        
        final long expectedTotalTransactionCount = expectedSampledNewCount + expectedUnsampledNewCount + expectedSampledContinuationCount + expectedUnsampledContinuationCount;

        Assert.assertEquals(expectedSampledNewCount, transactionCounter.getSampledNewCount());
        Assert.assertEquals(expectedUnsampledNewCount, transactionCounter.getUnSampledNewCount());
        Assert.assertEquals(expectedSampledContinuationCount, transactionCounter.getSampledContinuationCount());
        Assert.assertEquals(expectedUnsampledContinuationCount, transactionCounter.getUnSampledContinuationCount());
        Assert.assertEquals(expectedTotalTransactionCount, transactionCounter.getTotalTransactionCount());
    }
}
