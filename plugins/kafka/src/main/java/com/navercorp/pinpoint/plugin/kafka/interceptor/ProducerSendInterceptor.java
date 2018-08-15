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

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.RemoteAddressFieldAccessor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeader;


public class ProducerSendInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;


    public ProducerSendInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (logger.isDebugEnabled()) {
            logger.beforeInterceptor(target, args);
        }

        ProducerRecord record = getProducerRecord(args);
        if (record == null) {
            return;
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        if (trace.canSampled()) {
            SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(KafkaConstants.KAFKA_CLIENT);
            setPinpointHeaders(recorder, trace, record);
        } else {
            setPinpointHeaders(null, trace, record, false);
        }
    }

    private ProducerRecord getProducerRecord(Object args[]) {
        if (ArrayUtils.isEmpty(args)) {
            return null;
        }

        if (args[0] instanceof ProducerRecord) {
            return (ProducerRecord)args[0];
        }

        return null;
    }

    private void setPinpointHeaders(SpanEventRecorder recorder, Trace trace, ProducerRecord record) {
        setPinpointHeaders(recorder, trace, record, true);
    }

    private void setPinpointHeaders(SpanEventRecorder recorder, Trace trace, ProducerRecord record, boolean sample) {
        Headers kafkaHeaders = record.headers();
        if (kafkaHeaders == null) {
            return;
        }

        cleanPinpointHeader(kafkaHeaders);
        if (sample) {
            final TraceId nextId = trace.getTraceId().getNextTraceId();
            recorder.recordNextSpanId(nextId.getSpanId());

            kafkaHeaders.add(new RecordHeader(Header.HTTP_TRACE_ID.toString(), nextId.getTransactionId().getBytes()));
            kafkaHeaders.add(new RecordHeader(Header.HTTP_SPAN_ID.toString(), String.valueOf(nextId.getSpanId()).getBytes()));
            kafkaHeaders.add(new RecordHeader(Header.HTTP_PARENT_SPAN_ID.toString(), String.valueOf(nextId.getParentSpanId()).getBytes()));
            kafkaHeaders.add(new RecordHeader(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()).getBytes()));
            kafkaHeaders.add(new RecordHeader(Header.HTTP_PARENT_APPLICATION_NAME.toString(), String.valueOf(traceContext.getApplicationName()).getBytes()));
            kafkaHeaders.add(new RecordHeader(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), Short.toString(traceContext.getServerTypeCode()).getBytes()));
        } else {
            kafkaHeaders.add(new RecordHeader(Header.HTTP_SAMPLED.toString(), SamplingFlagUtils.SAMPLING_RATE_FALSE.getBytes()));
        }
    }

    private void cleanPinpointHeader(Headers kafkaHeaders) {
        Assert.requireNonNull(kafkaHeaders, "kafkaHeaders must not be null");

        for (org.apache.kafka.common.header.Header kafkaHeader : kafkaHeaders.toArray()) {
            String kafkaHeaderKey = kafkaHeader.key();
            if (Header.startWithPinpointHeader(kafkaHeaderKey)) {
                kafkaHeaders.remove(kafkaHeaderKey);
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        ProducerRecord record = getProducerRecord(args);
        if (record == null) {
            return;
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        if (!trace.canSampled()) {
            return;
        }

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);

            if (throwable != null) {
                recorder.recordException(throwable);
            } else {
                String remoteAddress = getRemoteAddress(target);
                recorder.recordEndPoint(remoteAddress);

                String topic = record.topic();
                recorder.recordDestinationId("topic:" + topic);
                recorder.recordAttribute(KafkaConstants.KAFKA_TOPIC_ANNOTATION_KEY, topic);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    private String getRemoteAddress(Object remoteAddressFieldAccessor) {
        String remoteAddress = null;
        if (remoteAddressFieldAccessor instanceof RemoteAddressFieldAccessor) {
            remoteAddress = ((RemoteAddressFieldAccessor) remoteAddressFieldAccessor)._$PINPOINT$_getRemoteAddress();
        }

        if (StringUtils.isEmpty(remoteAddress)) {
            return KafkaConstants.UNKNOWN;
        } else {
            return remoteAddress;
        }
    }


}
