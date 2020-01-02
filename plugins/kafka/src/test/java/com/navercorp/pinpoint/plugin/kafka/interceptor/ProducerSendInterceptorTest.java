package com.navercorp.pinpoint.plugin.kafka.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.RemoteAddressFieldAccessor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
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
    private TraceId traceId;

    @Mock
    private TraceId nextId;

    @Mock
    private SpanEventRecorder recorder;

    @Mock
    private ProducerRecord record;

    @Mock
    private Headers headers;

    @Mock
    private RemoteAddressFieldAccessor addressFieldAccessor;

    @Test
    public void before() {

        doReturn(trace).when(traceContext).currentRawTraceObject();
        doReturn(true).when(trace).canSampled();
        doReturn(traceId).when(trace).getTraceId();
        doReturn(nextId).when(traceId).getNextTraceId();
        doReturn(recorder).when(trace).traceBlockBegin();
        doReturn(headers).when(record).headers();
        doReturn(new Header[]{}).when(headers).toArray();
        doReturn("test").when(nextId).getTransactionId();
        doReturn(0l).when(nextId).getSpanId();
        doReturn(0l).when(nextId).getParentSpanId();
        short s = 0;
        doReturn(s).when(nextId).getFlags();

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