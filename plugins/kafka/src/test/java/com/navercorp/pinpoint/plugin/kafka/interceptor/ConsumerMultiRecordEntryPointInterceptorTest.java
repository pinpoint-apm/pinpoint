package com.navercorp.pinpoint.plugin.kafka.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Iterator;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ConsumerMultiRecordEntryPointInterceptorTest {

    @Mock
    private TraceContext traceContext;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private Trace trace;

    @Mock
    private SpanRecorder recorder;

    @Mock
    private ConsumerRecords consumerRecords;

    @Mock
    private ConsumerRecord consumerRecord;

    @Mock
    private Iterator iterator;

    @Test
    public void createTrace() {

        doReturn(trace).when(traceContext).newTraceObject();
        doReturn(true).when(trace).canSampled();
        doReturn(recorder).when(trace).getSpanRecorder();
        doReturn(1).when(consumerRecords).count();
        doReturn(iterator).when(consumerRecords).iterator();
        doReturn(consumerRecord).when(iterator).next();
        doReturn("Test").when(consumerRecord).topic();

        ConsumerMultiRecordEntryPointInterceptor interceptor = new ConsumerMultiRecordEntryPointInterceptor(traceContext, descriptor);
        interceptor.createTrace(new Object(), new Object[]{consumerRecords});

        verify(recorder).recordAcceptorHost("topic:Test");
        verify(recorder).recordAttribute(KafkaConstants.KAFKA_TOPIC_ANNOTATION_KEY, "Test");
        verify(recorder).recordAttribute(KafkaConstants.KAFKA_BATCH_ANNOTATION_KEY, 1);
        verify(recorder).recordRpcName("kafka://topic=Test?batch=1");
    }
}