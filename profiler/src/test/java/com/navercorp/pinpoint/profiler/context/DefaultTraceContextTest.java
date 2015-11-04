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

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.common.util.TransactionId;
import com.navercorp.pinpoint.common.util.TransactionIdUtils;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.context.DefaultTraceContext;
import com.navercorp.pinpoint.profiler.context.DefaultTraceId;
import com.navercorp.pinpoint.profiler.context.TransactionCounter.SamplingType;
import com.navercorp.pinpoint.profiler.context.storage.LogStorageFactory;
import com.navercorp.pinpoint.profiler.metadata.LRUCache;
import com.navercorp.pinpoint.profiler.sampler.SamplingRateSampler;
import com.navercorp.pinpoint.profiler.util.RuntimeMXBeanUtils;
import com.navercorp.pinpoint.test.TestAgentInformation;

import org.junit.Assert;
import org.junit.Test;
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
        TraceId traceID = new DefaultTraceId(agent, agentStartTime, agentTransactionCount);

        String id = traceID.getTransactionId();
        logger.info("id={}", id);

        TransactionId transactionid = TransactionIdUtils.parseTransactionId(id);

        Assert.assertEquals(transactionid.getAgentId(), agent);
        Assert.assertEquals(transactionid.getAgentStartTime(), agentStartTime);
        Assert.assertEquals(transactionid.getTransactionSequence(), agentTransactionCount);
    }

    @Test
    public void disableTrace() {
        DefaultTraceContext traceContext = new DefaultTraceContext(new TestAgentInformation());
        Trace trace = traceContext.disableSampling();
        Assert.assertNotNull(trace);
        Assert.assertFalse(trace.canSampled());

        traceContext.removeTraceObject();
    }

    @Test
    public void threadLocalBindTest() {
        final AgentInformation agentInformation = new TestAgentInformation();
        DefaultTraceContext traceContext1 = new DefaultTraceContext(agentInformation);
        Assert.assertNotNull(traceContext1.newTraceObject());

        DefaultTraceContext traceContext2 = new DefaultTraceContext(agentInformation);
        Trace notExist = traceContext2.currentRawTraceObject();
        Assert.assertNull(notExist);

        Assert.assertNotNull(traceContext1.currentRawTraceObject());
        traceContext1.removeTraceObject();
        Assert.assertNull(traceContext1.currentRawTraceObject());
    }
    
    @Test
    public void transactionCountTest() {
        final int samplingRate = 5;
        final Sampler sampler = new SamplingRateSampler(samplingRate);
        final DefaultTraceContext traceContext = new DefaultTraceContext(
                LRUCache.DEFAULT_CACHE_SIZE,
                new TestAgentInformation(),
                new LogStorageFactory(),
                sampler,
                new DefaultServerMetaDataHolder(RuntimeMXBeanUtils.getVmArgs()),
                true);
        final TransactionCounter transactionCounter = traceContext.getTransactionCounter();

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
        
        Assert.assertEquals(expectedSampledNewCount, transactionCounter.getTransactionCount(SamplingType.SAMPLED_NEW));
        Assert.assertEquals(expectedUnsampledNewCount, transactionCounter.getTransactionCount(SamplingType.UNSAMPLED_NEW));
        Assert.assertEquals(expectedSampledContinuationCount, transactionCounter.getTransactionCount(SamplingType.SAMPLED_CONTINUATION));
        Assert.assertEquals(expectedUnsampledContinuationCount, transactionCounter.getTransactionCount(SamplingType.UNSAMPLED_CONTINUATION));
        Assert.assertEquals(expectedTotalTransactionCount, transactionCounter.getTotalTransactionCount());
    }
}
