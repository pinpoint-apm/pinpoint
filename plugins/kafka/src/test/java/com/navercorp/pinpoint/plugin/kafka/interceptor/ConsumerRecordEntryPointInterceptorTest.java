package com.navercorp.pinpoint.plugin.kafka.interceptor;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ConsumerRecordEntryPointInterceptorTest {

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

    @Mock
    private SpanEventRecorder eventRecorder;

    @Mock
    private ConsumerRecord consumerRecord;

    @Mock
    private Headers headers;

    @Test
    public void doInBeforeTrace() {
        doReturn(profilerConfig).when(traceContext).getProfilerConfig();

        ConsumerRecordEntryPointInterceptor interceptor = new ConsumerRecordEntryPointInterceptor(traceContext, descriptor, 0);

        interceptor.doInBeforeTrace(eventRecorder, new Object(), new Object[]{});

        verify(eventRecorder).recordServiceType(KafkaConstants.KAFKA_CLIENT_INTERNAL);
    }

    @Test
    public void doInAfterTrace() {
        doReturn(profilerConfig).when(traceContext).getProfilerConfig();

        ConsumerRecordEntryPointInterceptor interceptor = new ConsumerRecordEntryPointInterceptor(traceContext, descriptor, 0);

        interceptor.doInAfterTrace(eventRecorder, new Object(), new Object[]{}, null, null);

        verify(eventRecorder).recordApi(descriptor);
        verify(eventRecorder).recordException(null);
    }

    @Test
    public void createTrace() {

        doReturn(trace).when(traceContext).newTraceObject();
        doReturn(profilerConfig).when(traceContext).getProfilerConfig();
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