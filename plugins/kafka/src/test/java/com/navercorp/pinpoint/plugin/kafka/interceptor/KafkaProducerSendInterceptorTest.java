/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kafka.interceptor;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.*;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class KafkaProducerSendInterceptorTest {
    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private SpanRecorder recorder;

    @Mock
    private SpanEventRecorder eventRecorder;

    @Mock
    private ProfilerConfig profilerConfig;

    @Mock
    private Trace trace;

    @Mock
    private TraceId traceId;

    @Mock
    private TraceId nextId;

    @Mock
    private ProducerRecord record;

    private KafkaProducerSendInterceptor interceptor;

    @Before
    public void setUp() {
        doReturn(new RecordHeaders()).when(record).headers();
        doReturn(nextId).when(traceId).getNextTraceId();
        doReturn(traceId).when(trace).getTraceId();
        doReturn(eventRecorder).when(trace).traceBlockBegin();
        doReturn(eventRecorder).when(trace).currentSpanEventRecorder();
        doReturn(profilerConfig).when(traceContext).getProfilerConfig();
        doReturn(trace).when(traceContext).currentTraceObject();
        doReturn(true).when(trace).canSampled();
        doReturn("^1517980646491").when(nextId).getTransactionId();
        doReturn("TEST").when(traceContext).getApplicationName();
        doReturn((short) 0).when(traceContext).getServerTypeCode();
        doReturn("TEST").when(profilerConfig).readString("profiler.kafka.caller", "CALLER");
        interceptor = new KafkaProducerSendInterceptor(traceContext, descriptor);
    }

    @Test
    public void before() {
        interceptor.before("TETS", new Object[]{record});
        assertEquals(record.headers().toArray().length, 6);
        verify(eventRecorder).recordEndPoint("TEST");
        verify(eventRecorder).recordDestinationId("TEST");
    }

    @Test
    public void after() {
        interceptor.after(null, null, null, null);
        verify(eventRecorder).recordException(null);
    }


}
