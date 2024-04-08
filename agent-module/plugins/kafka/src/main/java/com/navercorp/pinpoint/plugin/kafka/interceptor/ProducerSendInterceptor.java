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
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.kafka.KafkaConfig;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;
import com.navercorp.pinpoint.plugin.kafka.field.accessor.RemoteAddressFieldAccessor;
import com.navercorp.pinpoint.plugin.kafka.recorder.DefaultHeaderRecorder;
import com.navercorp.pinpoint.plugin.kafka.recorder.HeaderRecorder;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * @author Taejin Koo
 */
public class ProducerSendInterceptor implements AroundInterceptor {

    private final PluginLogger logger = PluginLogManager.getLogger(getClass());

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final HeaderRecorder headerRecorder;
    private final boolean isHeaderRecorded;

    public ProducerSendInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;

        KafkaConfig config = new KafkaConfig(traceContext.getProfilerConfig());
        this.isHeaderRecorded = config.isHeaderRecorded();
        this.headerRecorder = new DefaultHeaderRecorder();
    }

    @Override
    public void before(Object target, Object[] args) {
        if (logger.isDebugEnabled()) {
            logger.beforeInterceptor(target, args);
        }

        ProducerRecord<?, ?> record = ArrayArgumentUtils.getArgument(args, 0, ProducerRecord.class);
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


    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (logger.isDebugEnabled()) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        ProducerRecord<?, ?> record = ArrayArgumentUtils.getArgument(args, 0, ProducerRecord.class);
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

            if (isHeaderRecorded) {
                headerRecorder.record(recorder, record);
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

        return StringUtils.defaultIfEmpty(remoteAddress, KafkaConstants.UNKNOWN);
    }

}
