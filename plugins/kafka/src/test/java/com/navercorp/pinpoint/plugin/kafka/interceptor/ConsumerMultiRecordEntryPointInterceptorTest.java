package com.navercorp.pinpoint.plugin.kafka.interceptor;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.plugin.kafka.KafkaConfig;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;
import com.navercorp.pinpoint.plugin.kafka.KafkaPluginTestUtils;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ConsumerMultiRecordEntryPointInterceptorTest {

    @Mock
    private TraceContext traceContext;

    @Mock
    private ProfilerConfig profilerConfig;

    @Mock
    private MethodDescriptor descriptor;

    @Mock
    private Trace trace;

    @Mock
    private SpanRecorder recorder;

    @Test
    public void createTraceTest1() {
        prepareMock();

        final List<ConsumerRecord> consumerRecordList = KafkaPluginTestUtils.createConsumerRecordList("Test");
        final ConsumerMultiRecordEntryPointInterceptor interceptor = new ConsumerMultiRecordEntryPointInterceptor(traceContext, descriptor, 0);
        interceptor.createTrace(new Object(), new Object[]{consumerRecordList});
        assertCreateTrace(false, consumerRecordList);
    }

    @Test
    public void createTraceTest2() {
        prepareMock();

        final List<ConsumerRecord> consumerRecordList = KafkaPluginTestUtils.createConsumerRecordList("Test", "Test2");
        final ConsumerMultiRecordEntryPointInterceptor interceptor = new ConsumerMultiRecordEntryPointInterceptor(traceContext, descriptor, 0);
        interceptor.createTrace(new Object(), new Object[]{consumerRecordList});
        assertCreateTrace(false, consumerRecordList);
    }

    @MockitoSettings(strictness = Strictness.LENIENT)
    @Test
    public void filteredTest1() {
        final String topic1 = KafkaPluginTestUtils.makeTopicString(10);
        final String topic2 = KafkaPluginTestUtils.makeTopicString(10);
        prepareMock(Arrays.asList(topic1, topic2));

        final List<ConsumerRecord> consumerRecordList = KafkaPluginTestUtils.createConsumerRecordList(topic1, topic2);
        ConsumerMultiRecordEntryPointInterceptor interceptor = new ConsumerMultiRecordEntryPointInterceptor(traceContext, descriptor, 0);
        interceptor.createTrace(new Object(), new Object[]{consumerRecordList});
        assertCreateTrace(true, consumerRecordList);
    }

    @MockitoSettings(strictness = Strictness.LENIENT)
    @Test
    public void filteredTest2() {
        final String topic1 = KafkaPluginTestUtils.makeTopicString(10);
        final String topic2 = KafkaPluginTestUtils.makeTopicString(10);
        prepareMock(Arrays.asList(topic1, topic2));

        final List<ConsumerRecord> consumerRecordList = KafkaPluginTestUtils.createConsumerRecordList(topic2, topic2);
        ConsumerMultiRecordEntryPointInterceptor interceptor = new ConsumerMultiRecordEntryPointInterceptor(traceContext, descriptor, 0);
        interceptor.createTrace(new Object(), new Object[]{consumerRecordList});
        assertCreateTrace(true, consumerRecordList);
    }

    @MockitoSettings(strictness = Strictness.LENIENT)
    @Test
    public void notFilteredTest() {
        final String topic1 = KafkaPluginTestUtils.makeTopicString(10);
        final String topic2 = KafkaPluginTestUtils.makeTopicString(10);
        prepareMock(Arrays.asList(topic1, topic2));

        final List<ConsumerRecord> consumerRecordList = KafkaPluginTestUtils.createConsumerRecordList(topic1, "invalid_topic1");
        ConsumerMultiRecordEntryPointInterceptor interceptor = new ConsumerMultiRecordEntryPointInterceptor(traceContext, descriptor, 0);
        interceptor.createTrace(new Object(), new Object[]{consumerRecordList});
        assertCreateTrace(false, consumerRecordList);
    }

    private void prepareMock() {
        prepareMock(null);
    }

    private void prepareMock(List<String> topicNameList) {
        if (topicNameList != null) {
            final String collectedTopicNames = String.join(", ", topicNameList);
            doReturn(collectedTopicNames).when(profilerConfig).readString(KafkaConfig.EXCLUDE_CONSUMER_TOPIC, "");
        }
        doReturn(trace).when(traceContext).newTraceObject();
        doReturn(profilerConfig).when(traceContext).getProfilerConfig();
        doReturn(true).when(trace).canSampled();
        doReturn(recorder).when(trace).getSpanRecorder();
    }

    private void assertCreateTrace(boolean filtered, List<ConsumerRecord> consumerRecordList) {
        final String expectedTopicName = ConsumerRecordsDesc.create(consumerRecordList).getTopicString();
        final int expectedTopicSize = CollectionUtils.nullSafeSize(consumerRecordList);
        if (filtered) {
            verify(recorder, never()).recordAcceptorHost("Unknown");
            verify(recorder, never()).recordAttribute(KafkaConstants.KAFKA_TOPIC_ANNOTATION_KEY, expectedTopicName);
            verify(recorder, never()).recordAttribute(KafkaConstants.KAFKA_BATCH_ANNOTATION_KEY, expectedTopicSize);
            verify(recorder, never()).recordRpcName("kafka://topic=" + expectedTopicName + "?batch=" + expectedTopicSize);
        } else {
            verify(recorder).recordAcceptorHost("Unknown");
            verify(recorder).recordAttribute(KafkaConstants.KAFKA_TOPIC_ANNOTATION_KEY, expectedTopicName);
            verify(recorder).recordAttribute(KafkaConstants.KAFKA_BATCH_ANNOTATION_KEY, expectedTopicSize);
            verify(recorder).recordRpcName("kafka://topic=" + expectedTopicName + "?batch=" + expectedTopicSize);
        }
    }

}