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
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.pulsar.PulsarClientUtils;
import com.navercorp.pinpoint.plugin.pulsar.PulsarConstants;
import org.apache.pulsar.client.api.Producer;

/**
 * ProducerImpl.sendAsync(Message, SendCallback): records the producer event and opens the async context of the
 * send callback. The trace headers were already added by {@link MessageBuilderSendAsyncInterceptor}, which passes
 * the next trace id through the scope.
 *
 * @author zhouzixin@apache.org
 */
public class SendAsyncInterceptor implements AroundInterceptor {

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MethodDescriptor methodDescriptor;
    private final TraceContext traceContext;
    private final InterceptorScope scope;

    public SendAsyncInterceptor(MethodDescriptor methodDescriptor, TraceContext traceContext, InterceptorScope scope) {
        this.methodDescriptor = methodDescriptor;
        this.traceContext = traceContext;
        this.scope = scope;
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

        // No implementation types here: the shaded pulsar-client 5.x relocates them
        final String url = PulsarClientUtils.getServiceUrl(target);
        if (url != null) {
            recorder.recordEndPoint(url);
            recorder.recordDestinationId(url);
        }
        if (target instanceof Producer) {
            final String topic = ((Producer<?>) target).getTopic();
            recorder.recordAttribute(PulsarConstants.PULSAR_TOPIC_ANNOTATION_KEY, topic);
        }
        if (url != null) {
            recorder.recordAttribute(PulsarConstants.PULSAR_BROKER_URL_ANNOTATION_KEY, url);
        }

        final TraceId nextTraceId = getNextTraceId();
        if (nextTraceId != null) {
            recorder.recordNextSpanId(nextTraceId.getSpanId());
        }
    }

    // Set by MessageBuilderSendAsyncInterceptor when the message went through TypedMessageBuilderImpl.sendAsync()
    private TraceId getNextTraceId() {
        final InterceptorScopeInvocation invocation = scope.getCurrentInvocation();
        if (invocation == null) {
            return null;
        }
        final Object attachment = invocation.getAttachment();
        if (attachment instanceof TraceId) {
            invocation.removeAttachment();
            return (TraceId) attachment;
        }
        return null;
    }
}
