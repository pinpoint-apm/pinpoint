/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.common.profiler.util.TransactionId;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceId;
import com.navercorp.pinpoint.profiler.context.id.DefaultTransactionCounter;
import com.navercorp.pinpoint.profiler.context.id.IdGenerator;
import com.navercorp.pinpoint.profiler.context.id.TransactionCounter;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.context.provider.sampler.SamplerConfig;
import com.navercorp.pinpoint.profiler.sampler.CountingSamplerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.spy;

/**
 * @author emeroad
 * @author HyunGil Jeong
 */
public class DefaultTraceContextTest {


    @Test
    public void disableTrace() {
        ProfilerConfig profilerConfig = getProfilerConfig();

        try (DefaultApplicationContext applicationContext = MockTraceContextFactory.newMockApplicationContext(profilerConfig)) {
            applicationContext.start();

            TraceContext traceContext = applicationContext.getTraceContext();
            Trace trace = traceContext.disableSampling();
            Assertions.assertNotNull(trace);
            Assertions.assertFalse(trace.canSampled());

            traceContext.removeTraceObject();
        }
    }

    @Test
    public void threadLocalBindTest() {
        ProfilerConfig profilerConfig = getProfilerConfig();
        try (DefaultApplicationContext applicationContext = MockTraceContextFactory.newMockApplicationContext(profilerConfig);
             DefaultApplicationContext applicationContext2 = MockTraceContextFactory.newMockApplicationContext(profilerConfig)) {
            applicationContext.start();
            TraceContext traceContext = applicationContext.getTraceContext();
            Assertions.assertNotNull(traceContext.newTraceObject());

            applicationContext2.start();

            TraceContext traceContext2 = applicationContext2.getTraceContext();
            Trace notExist = traceContext2.currentRawTraceObject();


            Assertions.assertNull(notExist);

            Assertions.assertNotNull(traceContext.currentRawTraceObject());
            traceContext.removeTraceObject();
            Assertions.assertNull(traceContext.currentRawTraceObject());
        }
    }

    @Test
    public void transactionCountTest() {
        final int samplingRate = 5;

        final ProfilerConfig profilerConfig = getProfilerConfig();

        Mockito.when((profilerConfig.readInt(CountingSamplerFactory.LEGACY_SAMPLING_RATE_NAME, -1))).thenReturn(samplingRate);
        Mockito.when((profilerConfig.readBoolean(SamplerConfig.SAMPLER_ENABLE_NAME, true))).thenReturn(true);

        try (DefaultApplicationContext customContext = MockTraceContextFactory.newMockApplicationContext(profilerConfig)) {
            customContext.start();

            final TraceContext traceContext = customContext.getTraceContext();
            IdGenerator idGenerator = customContext.getInjector().getInstance(IdGenerator.class);
            final TransactionCounter transactionCounter = new DefaultTransactionCounter(idGenerator);


            final long newTransactionCount = 12;
            @SuppressWarnings("unused")
            final long expectedSampledNewCount = newTransactionCount / samplingRate + (newTransactionCount % samplingRate > 0 ? 1 : 0);
            final long expectedUnsampledNewCount = newTransactionCount - expectedSampledNewCount;
            for (int i = 0; i < newTransactionCount; i++) {
                traceContext.newTraceObject();
                traceContext.removeTraceObject();
            }

            final long expectedSampledContinuationCount = 5L;
            for (int i = 0; i < expectedSampledContinuationCount; i++) {
                traceContext.continueTraceObject(new DefaultTraceId(TransactionId.of("agentId", 0L, i)));
                traceContext.removeTraceObject();
            }

            final long expectedUnsampledContinuationCount = 10L;
            for (int i = 0; i < expectedUnsampledContinuationCount; i++) {
                traceContext.disableSampling();
                traceContext.removeTraceObject();
            }

            final long expectedTotalTransactionCount = expectedSampledNewCount + expectedUnsampledNewCount + expectedSampledContinuationCount + expectedUnsampledContinuationCount;

            Assertions.assertEquals(expectedSampledNewCount, transactionCounter.getSampledNewCount());
            Assertions.assertEquals(expectedUnsampledNewCount, transactionCounter.getUnSampledNewCount());
            Assertions.assertEquals(expectedSampledContinuationCount, transactionCounter.getSampledContinuationCount());
            Assertions.assertEquals(expectedUnsampledContinuationCount, transactionCounter.getUnSampledContinuationCount());
            Assertions.assertEquals(expectedTotalTransactionCount, transactionCounter.getTotalTransactionCount());
        }
    }

    public ProfilerConfig getProfilerConfig() {
        return spy(new DefaultProfilerConfig());
    }
}
