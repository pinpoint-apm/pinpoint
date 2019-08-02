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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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

    @Test
    public void createTraceTest1() {
        List<ConsumerRecord> consumerRecordList = new ArrayList<ConsumerRecord>();
        consumerRecordList.add(new ConsumerRecord("Test", 1, 1, "hello", "hello too"));

        doReturn(trace).when(traceContext).newTraceObject();
        doReturn(true).when(trace).canSampled();
        doReturn(recorder).when(trace).getSpanRecorder();
        doReturn(consumerRecordList.iterator()).when(consumerRecords).iterator();

        ConsumerMultiRecordEntryPointInterceptor interceptor = new ConsumerMultiRecordEntryPointInterceptor(traceContext, descriptor, 0);
        interceptor.createTrace(new Object(), new Object[]{consumerRecords});

        verify(recorder).recordAcceptorHost("Unknown");
        verify(recorder).recordAttribute(KafkaConstants.KAFKA_TOPIC_ANNOTATION_KEY, "Test");
        verify(recorder).recordAttribute(KafkaConstants.KAFKA_BATCH_ANNOTATION_KEY, 1);
        verify(recorder).recordRpcName("kafka://topic=Test?batch=1");
    }

    @Test
    public void createTraceTest2() {
        List<ConsumerRecord> consumerRecordList = new ArrayList<ConsumerRecord>();
        consumerRecordList.add(new ConsumerRecord("Test", 1, 1, "hello", "hello too"));
        consumerRecordList.add(new ConsumerRecord("Test2", 2, 1, "hello2", "hello too2"));

        doReturn(trace).when(traceContext).newTraceObject();
        doReturn(true).when(trace).canSampled();
        doReturn(recorder).when(trace).getSpanRecorder();
        doReturn(consumerRecordList.iterator()).when(consumerRecords).iterator();

        ConsumerMultiRecordEntryPointInterceptor interceptor = new ConsumerMultiRecordEntryPointInterceptor(traceContext, descriptor, 0);
        interceptor.createTrace(new Object(), new Object[]{consumerRecords});

        verify(recorder).recordAcceptorHost("Unknown");
        verify(recorder).recordAttribute(KafkaConstants.KAFKA_TOPIC_ANNOTATION_KEY, "[Test, Test2]");
        verify(recorder).recordAttribute(KafkaConstants.KAFKA_BATCH_ANNOTATION_KEY, 2);
        verify(recorder).recordRpcName("kafka://topic=[Test, Test2]?batch=2");
    }

}