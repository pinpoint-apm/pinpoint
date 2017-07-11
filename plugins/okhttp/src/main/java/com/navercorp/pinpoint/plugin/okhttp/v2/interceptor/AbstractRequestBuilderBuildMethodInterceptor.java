/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.okhttp.v2.interceptor;

import com.navercorp.pinpoint.bootstrap.context.Header;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.squareup.okhttp.Request;

/**
 * @author jaehong.kim
 */
public abstract class AbstractRequestBuilderBuildMethodInterceptor implements AroundInterceptor {

    protected final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    protected final TraceContext traceContext;
    protected final MethodDescriptor methodDescriptor;
    protected final InterceptorScope interceptorScope;

    public AbstractRequestBuilderBuildMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, InterceptorScope interceptorScope) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
        this.interceptorScope = interceptorScope;
    }

    abstract String toHost(Object target);

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        try {
            if (!(target instanceof Request.Builder)) {
                return;
            }

            final Request.Builder builder = ((Request.Builder) target);
            if (!trace.canSampled()) {
                builder.header(Header.HTTP_SAMPLED.toString(), SamplingFlagUtils.SAMPLING_RATE_FALSE);
                if (isDebug) {
                    logger.debug("Set HTTP header. sampled=false");
                }
                return;
            }

            final InterceptorScopeInvocation invocation = interceptorScope.getCurrentInvocation();
            final Object attachment = getAttachment(invocation);
            if (!(attachment instanceof TraceId)) {
                if (isDebug) {
                    logger.debug("Invalid interceptor scope invocation. {}", invocation);
                }
                return;
            }

            final TraceId nextId = (TraceId) attachment;
            builder.header(Header.HTTP_TRACE_ID.toString(), nextId.getTransactionId());
            builder.header(Header.HTTP_SPAN_ID.toString(), String.valueOf(nextId.getSpanId()));
            builder.header(Header.HTTP_PARENT_SPAN_ID.toString(), String.valueOf(nextId.getParentSpanId()));
            builder.header(Header.HTTP_FLAGS.toString(), String.valueOf(nextId.getFlags()));
            builder.header(Header.HTTP_PARENT_APPLICATION_NAME.toString(), traceContext.getApplicationName());
            builder.header(Header.HTTP_PARENT_APPLICATION_TYPE.toString(), Short.toString(traceContext.getServerTypeCode()));
            if (isDebug) {
                logger.debug("Set HTTP headers. traceId={}, spanId={}, parentSpanId={}", nextId.getTransactionId(), nextId.getSpanId(), nextId.getParentSpanId());
            }

            final String host = toHost(target);
            if (host != null) {
                builder.header(Header.HTTP_HOST.toString(), host);
                if (isDebug) {
                    logger.debug("Set HTTP header. host={}", host);
                }
            }
        } catch (Throwable t) {
            logger.warn("Failed to BEFORE process. {}", t.getMessage(), t);
        }
    }

    private Object getAttachment(InterceptorScopeInvocation invocation) {
        if (invocation == null) {
            return null;
        }
        return invocation.getAttachment();
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }
    }
}