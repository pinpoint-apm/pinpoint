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

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.plugin.kafka.KafkaConfig;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;
import com.navercorp.pinpoint.plugin.kafka.field.getter.ApiVersionsGetter;

/**
 * @author Taejin Koo
 */
public class ProducerAddHeaderInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private final DefaultHeaderSetter headerSetter = new DefaultHeaderSetter();

    private final TraceContext traceContext;

    private final boolean headerEnable;

    public ProducerAddHeaderInterceptor(TraceContext traceContext) {
        this.traceContext = traceContext;
        this.headerEnable = traceContext.getProfilerConfig().readBoolean(KafkaConfig.HEADER_ENABLE, true);
    }

    @Override
    public void before(Object target, Object[] args) {
        if (logger.isDebugEnabled()) {
            logger.beforeInterceptor(target, args);
        }

        if (!headerEnable) {
            return;
        }

        Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        if (!(target instanceof ApiVersionsGetter)) {
            return;

        }
        ApiVersionsGetter apiVersionsGetter = (ApiVersionsGetter) target;
        org.apache.kafka.clients.ApiVersions apiVersions = apiVersionsGetter._$PINPOINT$_getApiVersions();
        if (apiVersions == null || apiVersions.maxUsableProduceMagic() < org.apache.kafka.common.record.RecordBatch.MAGIC_VALUE_V2) {
            return;
        }

        if (!(args[0] instanceof org.apache.kafka.common.header.Headers)) {
            return;
        }

        org.apache.kafka.common.header.Headers headers = (org.apache.kafka.common.header.Headers) args[0];

        SpanEventRecorder spanEventRecorder = trace.currentSpanEventRecorder();
        headerSetter.setPinpointHeaders(spanEventRecorder, trace, headers, trace.canSampled(), traceContext.getApplicationName(), traceContext.getServerTypeCode());
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.afterInterceptor(target, args, result, throwable);
        }
    }

    private static class DefaultHeaderSetter {

        public void setPinpointHeaders(SpanEventRecorder recorder, Trace trace, org.apache.kafka.common.header.Headers headers, boolean sample, String applicationName, short serverTypeCode) {
            if (headers == null) {
                return;
            }

            cleanPinpointHeader(headers);
            if (sample) {
                final TraceId nextId = trace.getTraceId().getNextTraceId();
                recorder.recordNextSpanId(nextId.getSpanId());

                headers.add(new org.apache.kafka.common.header.internals.RecordHeader(Header.HTTP_TRACE_ID.toString(), nextId.getTransactionId().getBytes(KafkaConstants.DEFAULT_PINPOINT_HEADER_CHARSET)));
                headers.add(new org.apache.kafka.common.header.internals.RecordHeader(Header.HTTP_SPAN_ID.toString(), String.valueOf(nextId.getSpanId()).getBytes(KafkaConstants.DEFAULT_PINPOINT_HEADER_CHARSET)));
                headers.add(new org.apache.kafka.common.header.internals.RecordHeader(Header.HTTP_PARENT_SPAN_ID.toString(), String.valueOf(nextId.getParentSpanId()).getBytes(KafkaConstants.DEFAULT_PINPOINT_HEADER_CHARSET)));
                headers.add(new org.apache.kafka.common.header.internals.RecordHeader(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()).getBytes(KafkaConstants.DEFAULT_PINPOINT_HEADER_CHARSET)));
                headers.add(new org.apache.kafka.common.header.internals.RecordHeader(Header.HTTP_PARENT_APPLICATION_NAME.toString(), String.valueOf(applicationName).getBytes(KafkaConstants.DEFAULT_PINPOINT_HEADER_CHARSET)));
                headers.add(new org.apache.kafka.common.header.internals.RecordHeader(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), Short.toString(serverTypeCode).getBytes(KafkaConstants.DEFAULT_PINPOINT_HEADER_CHARSET)));
            } else {
                headers.add(new org.apache.kafka.common.header.internals.RecordHeader(Header.HTTP_SAMPLED.toString(), SamplingFlagUtils.SAMPLING_RATE_FALSE.getBytes(KafkaConstants.DEFAULT_PINPOINT_HEADER_CHARSET)));
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


