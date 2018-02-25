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
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class ConsumerRecordConstructorInterceptorTest {
    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private SpanRecorder recorder;

    @Mock
    private  SpanEventRecorder eventRecorder;

    @Mock
    private ProfilerConfig profilerConfig;

    @Mock
    private Trace trace;

    @Mock
    private TraceId traceId;

    private ConsumerRecordConstructorInterceptor interceptor;

    private Headers headers;

    @Before
    public void setUp() {
        doReturn(recorder).when(trace).getSpanRecorder();
        doReturn(traceId).when(traceContext).createTraceId(anyString(), anyLong(), anyLong(), anyShort());
        doReturn(trace).when(traceContext).continueTraceObject(traceId);
        doReturn(true).when(trace).canSampled();
        doReturn(profilerConfig).when(traceContext).getProfilerConfig();
        doReturn(trace).when(traceContext).currentRawTraceObject();
        doReturn(eventRecorder).when(trace).currentSpanEventRecorder();
        headers = createMockHeader();
        interceptor = new ConsumerRecordConstructorInterceptor(traceContext, descriptor);

    }

    private Headers createMockHeader() {
        Headers headers = new RecordHeaders();
        headers.add(new RecordHeader(Header.HTTP_TRACE_ID.toString(), "^1517980646491".getBytes()));
        headers.add(new RecordHeader(Header.HTTP_SPAN_ID.toString(), "-1918180025536006038".getBytes()));
        headers.add(new RecordHeader(Header.HTTP_PARENT_SPAN_ID.toString(), "-3022580112884983244".getBytes()));
        headers.add(new RecordHeader(Header.HTTP_FLAGS.toString(), "0".getBytes()));
        headers.add(new RecordHeader(Header.HTTP_PARENT_APPLICATION_NAME.toString(), "TEST".getBytes()));
        headers.add(new RecordHeader(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), "0".getBytes()));
        return headers;
    }

    @Test
    public void before() {
        interceptor.before("Test", new Object[]{null, null, null, null, null, null, null, null, null, null, headers});
        verify(recorder).recordServiceType(KafkaConstants.KAFKA);
        verify(recorder).recordApi(descriptor);
        verify(recorder).recordParentApplication("TEST", NumberUtils.parseShort("0", ServiceType.UNDEFINED.getCode()));
    }

    @Test
    public void after() {
        interceptor.after(null, null, null, null);
        verify(eventRecorder).recordException(null);
    }
}
