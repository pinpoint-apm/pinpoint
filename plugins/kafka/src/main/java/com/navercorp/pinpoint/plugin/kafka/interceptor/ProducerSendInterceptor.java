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

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicReference;


public class ProducerSendInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;

    private final AtomicReference<HeaderSetter> headerSetterReference = new AtomicReference<HeaderSetter>();

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

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        if (trace.canSampled()) {
            SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(KafkaConstants.KAFKA_CLIENT);
            setPinpointHeaders(recorder, trace, record, true);
        } else {
            setPinpointHeaders(null, trace, record, false);
        }
    }

    private ProducerRecord getProducerRecord(Object args[]) {
        if (ArrayUtils.isEmpty(args)) {
            return null;
        }

        if (args[0] instanceof ProducerRecord) {
            return (ProducerRecord) args[0];
        }

        return null;
    }

    private void setPinpointHeaders(SpanEventRecorder recorder, Trace trace, ProducerRecord record, boolean sample) {
        HeaderSetter headerSetter = headerSetterReference.get();
        if (headerSetter == null) {
            headerSetter = HeaderSetterProvider.get(record);
            headerSetterReference.compareAndSet(null, headerSetter);
        }
        headerSetter.setPinpointHeaders(recorder, trace, record, sample, traceContext.getApplicationName(), traceContext.getServerTypeCode());
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

            String remoteAddress = getRemoteAddress(target);
            recorder.recordEndPoint(remoteAddress);
            recorder.recordDestinationId(remoteAddress);

            String topic = record.topic();
            recorder.recordAttribute(KafkaConstants.KAFKA_TOPIC_ANNOTATION_KEY, topic);

            if (throwable != null) {
                recorder.recordException(throwable);
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

    private static class HeaderSetterProvider {

        static HeaderSetter get(Object object) {
            try {
                final Class<?> aClass = object.getClass();
                final Method method = aClass.getMethod("headers");
                if (method != null) {
                    return new DefaultHeaderSetter();
                }
            } catch (NoSuchMethodException e) {
                // ignore
            }
            return new DisabledHeaderSetter();
        }

    }

    private interface HeaderSetter {

        void setPinpointHeaders(SpanEventRecorder recorder, Trace trace, ProducerRecord record, boolean sample, String applicationName, short serverTypeCode);

    }

    private static class DisabledHeaderSetter implements HeaderSetter {

        @Override
        public void setPinpointHeaders(SpanEventRecorder recorder, Trace trace, ProducerRecord record, boolean sample, String applicationName, short serverTypeCode) {
        }

    }

    private static class DefaultHeaderSetter implements HeaderSetter {

        public void setPinpointHeaders(SpanEventRecorder recorder, Trace trace, ProducerRecord record, boolean sample, String applicationName, short serverTypeCode) {
            org.apache.kafka.common.header.Headers kafkaHeaders = record.headers();
            if (kafkaHeaders == null) {
                return;
            }

            cleanPinpointHeader(kafkaHeaders);
            if (sample) {
                final TraceId nextId = trace.getTraceId().getNextTraceId();
                recorder.recordNextSpanId(nextId.getSpanId());

                kafkaHeaders.add(new org.apache.kafka.common.header.internals.RecordHeader(Header.HTTP_TRACE_ID.toString(), nextId.getTransactionId().getBytes(KafkaConstants.DEFAULT_PINPOINT_HEADER_CHARSET)));
                kafkaHeaders.add(new org.apache.kafka.common.header.internals.RecordHeader(Header.HTTP_SPAN_ID.toString(), String.valueOf(nextId.getSpanId()).getBytes(KafkaConstants.DEFAULT_PINPOINT_HEADER_CHARSET)));
                kafkaHeaders.add(new org.apache.kafka.common.header.internals.RecordHeader(Header.HTTP_PARENT_SPAN_ID.toString(), String.valueOf(nextId.getParentSpanId()).getBytes(KafkaConstants.DEFAULT_PINPOINT_HEADER_CHARSET)));
                kafkaHeaders.add(new org.apache.kafka.common.header.internals.RecordHeader(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()).getBytes(KafkaConstants.DEFAULT_PINPOINT_HEADER_CHARSET)));
                kafkaHeaders.add(new org.apache.kafka.common.header.internals.RecordHeader(Header.HTTP_PARENT_APPLICATION_NAME.toString(), String.valueOf(applicationName).getBytes(KafkaConstants.DEFAULT_PINPOINT_HEADER_CHARSET)));
                kafkaHeaders.add(new org.apache.kafka.common.header.internals.RecordHeader(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), Short.toString(serverTypeCode).getBytes(KafkaConstants.DEFAULT_PINPOINT_HEADER_CHARSET)));
            } else {
                kafkaHeaders.add(new org.apache.kafka.common.header.internals.RecordHeader(Header.HTTP_SAMPLED.toString(), SamplingFlagUtils.SAMPLING_RATE_FALSE.getBytes(KafkaConstants.DEFAULT_PINPOINT_HEADER_CHARSET)));
            }
        }

        private void cleanPinpointHeader(org.apache.kafka.common.header.Headers kafkaHeaders) {
            Assert.requireNonNull(kafkaHeaders, "kafkaHeaders");

            for (org.apache.kafka.common.header.Header kafkaHeader : kafkaHeaders.toArray()) {
                String kafkaHeaderKey = kafkaHeader.key();
                if (Header.startWithPinpointHeader(kafkaHeaderKey)) {
                    kafkaHeaders.remove(kafkaHeaderKey);
                }
            }
        }

    }

}
