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

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.kafka.KafkaConfig;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;
import com.navercorp.pinpoint.plugin.kafka.encoder.PinpointValueEncoder;
import org.apache.kafka.common.header.Headers;

public class ConsumerRecordConstructorInterceptor implements AroundInterceptor {

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private static String CALL_SERVER;
    private final PLogger logger = PLoggerFactory.getLogger(ConsumerRecordConstructorInterceptor.class);

    public ConsumerRecordConstructorInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        KafkaConfig config = new KafkaConfig(traceContext.getProfilerConfig());
        PinpointValueEncoder.INSTANCE.init(config.getEncoder());
        CALL_SERVER = config.getCaller();
    }

    @Override
    public void before(Object target, Object[] args) {
        if (args[10] instanceof Headers) {
            Headers headers = (Headers) args[10];
            String transactionId = null, spanID = null, parentSpanID = null, parentApplicationName = null, parentApplicationType = null, flags = null;
            for (org.apache.kafka.common.header.Header header : headers.toArray()) {
                if (header.key().equals(Header.HTTP_TRACE_ID.toString())) {
                    transactionId = new String(header.value());
                } else if (header.key().equals(Header.HTTP_PARENT_SPAN_ID.toString())) {
                    parentSpanID = new String(header.value());
                } else if (header.key().equals(Header.HTTP_SPAN_ID.toString())) {
                    spanID = new String(header.value());
                } else if (header.key().equals(Header.HTTP_PARENT_APPLICATION_NAME.toString())) {
                    parentApplicationName = new String(header.value());
                } else if (header.key().equals(Header.HTTP_PARENT_APPLICATION_TYPE.toString())) {
                    parentApplicationType = new String(header.value());
                } else if (header.key().equals(Header.HTTP_FLAGS.toString())) {
                    flags = new String(header.value());
                }
            }
            if (transactionId != null && spanID != null && parentSpanID != null && parentApplicationName != null && parentApplicationType != null && flags != null) {
                TraceId traceId = traceContext.createTraceId(transactionId, Long.parseLong(parentSpanID), Long.parseLong(spanID), Short.parseShort(flags));
                Trace trace = traceContext.continueTraceObject(traceId);
                if (trace.canSampled()) {
                    final SpanRecorder recorder = trace.getSpanRecorder();
                    recorder.recordServiceType(KafkaConstants.KAFKA);
                    recorder.recordApi(descriptor);
                    recorder.recordParentApplication(parentApplicationName, NumberUtils.parseShort(parentApplicationType, ServiceType.UNDEFINED.getCode()));
                    recorder.recordAcceptorHost(CALL_SERVER);
                }
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordException(throwable);
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", t.getMessage(), t);
            }
        } finally {
            trace.traceBlockEnd();
            deleteTrace(trace);
        }
    }

    private void deleteTrace(final Trace trace) {
        traceContext.removeTraceObject();
        trace.close();
    }
}
