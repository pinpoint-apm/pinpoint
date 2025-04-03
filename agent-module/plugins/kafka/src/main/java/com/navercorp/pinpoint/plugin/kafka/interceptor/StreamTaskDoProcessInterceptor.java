/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanRecursiveAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceReader;
import com.navercorp.pinpoint.bootstrap.plugin.request.ServerRequestRecorder;
import com.navercorp.pinpoint.plugin.kafka.KafkaConfig;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;
import com.navercorp.pinpoint.plugin.kafka.descriptor.KafkaStreamsMethodDescriptor;
import com.navercorp.pinpoint.plugin.kafka.field.getter.StampedRecordGetter;
import com.navercorp.pinpoint.plugin.kafka.recorder.DefaultHeaderRecorder;
import com.navercorp.pinpoint.plugin.kafka.recorder.HeaderRecorder;
import com.navercorp.pinpoint.plugin.kafka.util.KafkaRequestAdaptor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.internals.StampedRecord;

public class StreamTaskDoProcessInterceptor extends SpanRecursiveAroundInterceptor {
    private static final String SCOPE_NAME = "##KAFKA_STREAMS_START_TRACE";
    private static final KafkaStreamsMethodDescriptor METHOD_DESCRIPTOR = new KafkaStreamsMethodDescriptor();

    private final boolean headerRecorded;
    private final HeaderRecorder headerRecorder;
    private final ServerRequestRecorder<ConsumerRecord> serverRequestRecorder;
    private final RequestTraceReader<ConsumerRecord> requestTraceReader;

    public StreamTaskDoProcessInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor, SCOPE_NAME);
        traceContext.cacheApi(METHOD_DESCRIPTOR);

        final KafkaConfig config = new KafkaConfig(traceContext.getProfilerConfig());
        this.headerRecorded = config.isHeaderRecorded();
        this.headerRecorder = new DefaultHeaderRecorder();

        final RequestAdaptor<ConsumerRecord> requestAdaptor = new KafkaRequestAdaptor();
        this.serverRequestRecorder = new ServerRequestRecorder<>(requestAdaptor);
        this.requestTraceReader = new RequestTraceReader<>(traceContext, requestAdaptor, false);
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(KafkaConstants.KAFKA_CLIENT_INTERNAL);
    }

    @Override
    public Trace createTrace(Object target, Object[] args) {
        if (Boolean.FALSE == (target instanceof StampedRecordGetter)) {
            return null;
        }

        final ConsumerRecord<?, ?> consumerRecord = getConsumerRecord(target);
        if (consumerRecord == null) {
            return null;
        }

        final Trace trace = requestTraceReader.read(consumerRecord);
        if (trace.canSampled()) {
            final SpanRecorder recorder = trace.getSpanRecorder();
            recorder.recordServiceType(KafkaConstants.KAFKA_STREAMS);
            recorder.recordApi(METHOD_DESCRIPTOR);
            recorder.recordAttribute(KafkaConstants.KAFKA_TOPIC_ANNOTATION_KEY, consumerRecord.topic());
            recorder.recordAttribute(KafkaConstants.KAFKA_PARTITION_ANNOTATION_KEY, consumerRecord.partition());
            recorder.recordAttribute(KafkaConstants.KAFKA_OFFSET_ANNOTATION_KEY, consumerRecord.offset());
            this.serverRequestRecorder.record(recorder, consumerRecord);
            if (this.headerRecorded) {
                this.headerRecorder.record(recorder, consumerRecord);
            }
        }

        return trace;
    }

    private ConsumerRecord<?, ?> getConsumerRecord(Object target) {
        final StampedRecord stampedRecord = ((StampedRecordGetter) target)._$PINPOINT$_getRecord();
        if (stampedRecord == null) {
            return null;
        }
        final ConsumerRecord<?, ?> consumerRecord = stampedRecord.value;
        if (consumerRecord == null) {
            return null;
        }
        return consumerRecord;
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);

        if (throwable != null) {
            recorder.recordException(throwable);
        }
    }
}