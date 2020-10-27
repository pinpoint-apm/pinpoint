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

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.RemoteAddressFieldAccessor;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ProducerSendInterceptorTest {

    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private Trace trace;

    @Mock
    private SpanEventRecorder recorder;

    @Mock
    private ProducerRecord record;

    @Mock
    private RemoteAddressFieldAccessor addressFieldAccessor;

    @Test
    public void before() {
        doReturn(trace).when(traceContext).currentRawTraceObject();
        doReturn(true).when(trace).canSampled();
        doReturn(recorder).when(trace).traceBlockBegin();

        ProducerSendInterceptor interceptor = new ProducerSendInterceptor(traceContext, descriptor);
        Object target = new Object();
        Object[] args = new Object[]{record};
        interceptor.before(target, args);

        verify(recorder).recordServiceType(KafkaConstants.KAFKA_CLIENT);

    }

    @Test
    public void after() {
        doReturn(trace).when(traceContext).currentTraceObject();
        doReturn(true).when(trace).canSampled();
        doReturn(recorder).when(trace).currentSpanEventRecorder();
        doReturn("test").when(record).topic();

        ProducerSendInterceptor interceptor = new ProducerSendInterceptor(traceContext, descriptor);
        Object[] args = new Object[]{record};
        interceptor.after(addressFieldAccessor, args, null, null);

        verify(recorder).recordEndPoint(KafkaConstants.UNKNOWN);
        verify(recorder).recordDestinationId("Unknown");
        verify(recorder).recordAttribute(KafkaConstants.KAFKA_TOPIC_ANNOTATION_KEY, "test");
    }
}