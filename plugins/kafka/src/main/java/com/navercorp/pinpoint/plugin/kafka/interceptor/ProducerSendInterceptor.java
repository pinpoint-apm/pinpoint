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

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.RemoteAddressFieldAccessor;

import org.apache.kafka.clients.producer.ProducerRecord;


/**
 * @author Taejin Koo
 */
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

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        if (trace.canSampled()) {
            SpanEventRecorder spanEventRecorder = trace.traceBlockBegin();
            spanEventRecorder.recordServiceType(KafkaConstants.KAFKA_CLIENT);
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


}
