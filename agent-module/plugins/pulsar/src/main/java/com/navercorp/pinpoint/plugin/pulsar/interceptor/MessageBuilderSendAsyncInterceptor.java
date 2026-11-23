/*
 * Copyright 2026 NAVER Corp.
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

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.plugin.pulsar.PulsarClientUtils;
import com.navercorp.pinpoint.plugin.pulsar.PulsarConstants;
import org.apache.pulsar.client.api.TypedMessageBuilder;

/**
 * TypedMessageBuilderImpl.sendAsync(), the method every send goes through before the message is built:
 * Producer.send(T), Producer.sendAsync(T) and TypedMessageBuilder.send() all end up here.
 * <p>
 * Adds the trace headers through the public TypedMessageBuilder.property(), and hands the next trace id over to
 * {@link SendAsyncInterceptor}, which records the producer event. An unsampled trace adds Pinpoint-Sampled=s0 only.
 */
public class MessageBuilderSendAsyncInterceptor implements AroundInterceptor {

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final InterceptorScope scope;

    public MessageBuilderSendAsyncInterceptor(TraceContext traceContext, InterceptorScope scope) {
        this.traceContext = traceContext;
        this.scope = scope;
    }

    @Override
    public void before(final Object target, final Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }
        if (!(target instanceof TypedMessageBuilder)) {
            return;
        }

        try {
            final TypedMessageBuilder<?> builder = (TypedMessageBuilder<?>) target;
            if (!trace.canSampled()) {
                // the consumer then skips the message instead of starting a new trace
                builder.property(Header.HTTP_SAMPLED.toString(), SamplingFlagUtils.SAMPLING_RATE_FALSE);
                return;
            }

            final TraceId nextTraceId = trace.getTraceId().getNextTraceId();

            builder.property(Header.HTTP_FLAGS.toString(), String.valueOf(nextTraceId.getFlags()));
            builder.property(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationName());
            builder.property(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), String.valueOf(traceContext.getServerTypeCode()));
            if (traceContext.getServiceName() != null) {
                builder.property(Header.HTTP_PARENT_SERVICE_NAME.toString(), traceContext.getServiceName());
            }
            builder.property(Header.HTTP_PARENT_SPAN_ID.toString(), String.valueOf(nextTraceId.getParentSpanId()));
            builder.property(Header.HTTP_SPAN_ID.toString(), String.valueOf(nextTraceId.getSpanId()));
            builder.property(Header.HTTP_TRACE_ID.toString(), nextTraceId.getTransactionId());
            final String url = PulsarClientUtils.getServiceUrl(target);
            if (url != null) {
                builder.property(PulsarConstants.ACCEPTOR_HOST, url);
            }
            builder.property(PulsarConstants.IS_ASYNC_SEND, String.valueOf(trace.isAsync()));

            final InterceptorScopeInvocation invocation = scope.getCurrentInvocation();
            if (invocation != null) {
                invocation.setAttachment(nextTraceId);
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

        final InterceptorScopeInvocation invocation = scope.getCurrentInvocation();
        if (invocation != null) {
            invocation.removeAttachment();
        }
    }
}
