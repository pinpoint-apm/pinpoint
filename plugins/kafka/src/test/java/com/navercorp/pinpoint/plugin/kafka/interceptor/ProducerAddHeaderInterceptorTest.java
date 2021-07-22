/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kafka.interceptor;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.plugin.kafka.KafkaConfig;
import com.navercorp.pinpoint.plugin.kafka.field.getter.ApiVersionsGetter;

import org.apache.kafka.clients.ApiVersions;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.RecordBatch;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.doReturn;

/**
 * @author Taejin Koo
 */
@RunWith(MockitoJUnitRunner.class)
public class ProducerAddHeaderInterceptorTest {

    @Mock
    private TraceContext traceContext;

    @Mock
    private ProfilerConfig profilerConfig;

    @Mock
    private Trace trace;

    @Mock
    private TraceId traceId;

    @Mock
    private TraceId nextId;

    @Mock
    private SpanEventRecorder recorder;

    @Mock
    private ApiVersionsGetter apiVersionsGetter;

    @Mock
    private ApiVersions apiVersions;

    @Test
    public void beforeWhenSampled() {
        doReturn(profilerConfig).when(traceContext).getProfilerConfig();
        doReturn(true).when(profilerConfig).readBoolean(KafkaConfig.HEADER_ENABLE, true);
        Header[] headers = getHeadersWhenSampled();
        Assert.assertEquals(6, headers.length);
    }

    @Test
    public void beforeWhenSampledNoHeader() {
        doReturn(profilerConfig).when(traceContext).getProfilerConfig();
        doReturn(false).when(profilerConfig).readBoolean(KafkaConfig.HEADER_ENABLE, true);

        Header[] headers = getHeadersWhenSampled();
        Assert.assertEquals(0, headers.length);
    }

    private Header[] getHeadersWhenSampled() {
        doReturn(trace).when(traceContext).currentRawTraceObject();
        doReturn(true).when(trace).canSampled();
        doReturn(traceId).when(trace).getTraceId();
        doReturn(nextId).when(traceId).getNextTraceId();
        doReturn(recorder).when(trace).currentSpanEventRecorder();

        doReturn(apiVersions).when(apiVersionsGetter)._$PINPOINT$_getApiVersions();
        doReturn(RecordBatch.MAGIC_VALUE_V2).when(apiVersions).maxUsableProduceMagic();

        doReturn("test").when(nextId).getTransactionId();
        doReturn(0L).when(nextId).getSpanId();
        doReturn(0L).when(nextId).getParentSpanId();

        short s = 0;
        doReturn(s).when(nextId).getFlags();

        ProducerAddHeaderInterceptor interceptor = new ProducerAddHeaderInterceptor(traceContext);

        RecordHeaders recordHeader = new RecordHeaders();
        Object[] args = new Object[]{recordHeader};
        interceptor.before(apiVersionsGetter, args);

        return recordHeader.toArray();
    }

    @Test
    public void beforeWhenUnsampled() {
        doReturn(profilerConfig).when(traceContext).getProfilerConfig();
        doReturn(true).when(profilerConfig).readBoolean(KafkaConfig.HEADER_ENABLE, true);
        doReturn(trace).when(traceContext).currentRawTraceObject();
        doReturn(false).when(trace).canSampled();
        doReturn(recorder).when(trace).currentSpanEventRecorder();

        doReturn(apiVersions).when(apiVersionsGetter)._$PINPOINT$_getApiVersions();
        doReturn(RecordBatch.MAGIC_VALUE_V2).when(apiVersions).maxUsableProduceMagic();

        ProducerAddHeaderInterceptor interceptor = new ProducerAddHeaderInterceptor(traceContext);

        RecordHeaders recordHeader = new RecordHeaders();
        Object[] args = new Object[]{recordHeader};
        interceptor.before(apiVersionsGetter, args);

        Header[] headers = recordHeader.toArray();
        Assert.assertEquals(1, headers.length);
    }

    @Test
    public void beforeWhenV1() {
        doReturn(profilerConfig).when(traceContext).getProfilerConfig();
        doReturn(true).when(profilerConfig).readBoolean(KafkaConfig.HEADER_ENABLE, true);
        doReturn(trace).when(traceContext).currentRawTraceObject();

        doReturn(apiVersions).when(apiVersionsGetter)._$PINPOINT$_getApiVersions();
        doReturn(RecordBatch.MAGIC_VALUE_V1).when(apiVersions).maxUsableProduceMagic();

        ProducerAddHeaderInterceptor interceptor = new ProducerAddHeaderInterceptor(traceContext);

        RecordHeaders recordHeader = new RecordHeaders();
        Object[] args = new Object[]{recordHeader};
        interceptor.before(apiVersionsGetter, args);

        Header[] headers = recordHeader.toArray();
        Assert.assertEquals(0, headers.length);
    }

}
