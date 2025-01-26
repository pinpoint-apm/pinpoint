/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.pulsar.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.pulsar.PulsarConstants;
import org.apache.pulsar.client.impl.MessageImpl;
import org.apache.pulsar.client.impl.ProducerImpl;

import java.util.HashMap;
import java.util.Map;

import static com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils.SAMPLING_RATE_FALSE;

/**
 * @author zhouzixin@apache.org
 */
public class SendAsyncInterceptor implements AroundInterceptor {

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MethodDescriptor methodDescriptor;
    private final TraceContext traceContext;

    public SendAsyncInterceptor(MethodDescriptor methodDescriptor, TraceContext traceContext) {
        this.methodDescriptor = methodDescriptor;
        this.traceContext = traceContext;
    }

    @Override
    public void before(final Object target, final Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            doInBeforeTrace(recorder, target, args);
            final AsyncContextAccessor asyncContextAccessor = ArrayArgumentUtils.getArgument(args, 1, AsyncContextAccessor.class);
            if (asyncContextAccessor != null) {
                final AsyncContext asyncContext = recorder.recordNextAsyncContext();
                asyncContextAccessor._$PINPOINT$_setAsyncContext(asyncContext);
            }
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to BEFORE process. {}", t.getMessage(), t);
            }
        }
    }

    @Override
    public void after(final Object target, final Object[] args, final Object result, final Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(methodDescriptor);
            if (throwable != null) {
                recorder.recordException(throwable);
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to AFTER process. {}", th.getMessage(), th);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    private void doInBeforeTrace(final SpanEventRecorder recorder, final Object target, final Object[] args) {
        recorder.recordServiceType(PulsarConstants.PULSAR_CLIENT);
        recorder.recordApi(methodDescriptor);

        ProducerImpl<?> producer = (ProducerImpl<?>) target;
        final String url = producer.getClient().getLookup().getServiceUrl(), topic = producer.getTopic();
        recorder.recordEndPoint(url);
        recorder.recordDestinationId(url);
        recorder.recordAttribute(PulsarConstants.PULSAR_TOPIC_ANNOTATION_KEY, topic);
        recorder.recordAttribute(PulsarConstants.PULSAR_BROKER_URL_ANNOTATION_KEY, url);

        final Trace trace = traceContext.currentRawTraceObject();
        final TraceId nextTraceId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextTraceId.getSpanId());
        final Map<String, Object> paramMap = new HashMap<>();
        if (trace.canSampled()) {
            paramMap.put(Header.HTTP_FLAGS.toString(), String.valueOf(nextTraceId.getFlags()));
            paramMap.put(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationName());
            paramMap.put(
                    Header.HTTP_PARENT_APPLICATION_TYPE.toString(),
                    String.valueOf(traceContext.getServerTypeCode())
            );
            paramMap.put(Header.HTTP_PARENT_SPAN_ID.toString(), String.valueOf(nextTraceId.getParentSpanId()));
            paramMap.put(Header.HTTP_SPAN_ID.toString(), String.valueOf(nextTraceId.getSpanId()));
            paramMap.put(Header.HTTP_TRACE_ID.toString(), nextTraceId.getTransactionId());
            paramMap.put(PulsarConstants.ACCEPTOR_HOST, url);
            paramMap.put(PulsarConstants.IS_ASYNC_SEND, trace.isAsync());
        } else {
            paramMap.put(Header.HTTP_SAMPLED.toString(), SAMPLING_RATE_FALSE);
        }
        final MessageImpl<?> message = (MessageImpl<?>) args[0];
        for (final Map.Entry<String, Object> entry : paramMap.entrySet()) {
            message.getMessageBuilder().addProperty()
                    .setKey(entry.getKey())
                    .setValue(String.valueOf(entry.getValue()));
        }
    }
}
