package com.navercorp.pinpoint.plugin.kafka.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerRecordEntryPointInterceptorTest {

    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private Trace trace;

    @Mock
    private SpanRecorder recorder;

    @Mock
    private SpanEventRecorder eventRecorder;

    @Mock
    private ConsumerRecord consumerRecord;

    @Mock
    private Headers headers;

    @Test
    public void doInBeforeTrace() {

        ConsumerRecordEntryPointInterceptor interceptor = new ConsumerRecordEntryPointInterceptor(traceContext, descriptor, 0);

        interceptor.doInBeforeTrace(eventRecorder, new Object(), new Object[]{});

        verify(eventRecorder).recordServiceType(KafkaConstants.KAFKA_CLIENT_INTERNAL);
    }

    @Test
    public void doInAfterTrace() {

        ConsumerRecordEntryPointInterceptor interceptor = new ConsumerRecordEntryPointInterceptor(traceContext, descriptor, 0);

        interceptor.doInAfterTrace(eventRecorder, new Object(), new Object[]{}, null, null);

        verify(eventRecorder).recordApi(descriptor);
        verify(eventRecorder).recordException(null);
    }

    @Test
    public void createTrace() {

        doReturn(trace).when(traceContext).newTraceObject();
        doReturn(true).when(trace).canSampled();
        doReturn(recorder).when(trace).getSpanRecorder();

        doReturn("Test").when(consumerRecord).topic();
        doReturn(1l).when(consumerRecord).offset();
        doReturn(0).when(consumerRecord).partition();
        doReturn(headers).when(consumerRecord).headers();
        doReturn(new Header[]{}).when(headers).toArray();

        ConsumerRecordEntryPointInterceptor interceptor = new ConsumerRecordEntryPointInterceptor(traceContext, descriptor, 0);

        interceptor.createTrace(new Object(), new Object[]{consumerRecord});

        verify(recorder).recordAcceptorHost("Unknown");
        verify(recorder).recordRpcName("kafka://topic=Test?partition=0&offset=1");

    }
}