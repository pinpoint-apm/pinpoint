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

package com.navercorp.pinpoint.plugin.okhttp.v3.interceptor;

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
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.plugin.okhttp.EndPointUtils;
import com.navercorp.pinpoint.plugin.okhttp.v3.HttpUrlGetter;
import okhttp3.HttpUrl;
import okhttp3.Request;

/**
 * @author jaehong.kim
 */
public class RequestBuilderBuildMethodInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;
    private final InterceptorScope interceptorScope;

    public RequestBuilderBuildMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, InterceptorScope interceptorScope) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
        this.interceptorScope = interceptorScope;
    }

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
                    logger.debug("Set HTTP headers. sampled=false");
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
                logger.debug("Set HTTP Headers. transactionId={}, spanId={}, parentSpanId={}", nextId.getTransactionId(), nextId.getSpanId(), nextId.getParentSpanId());
            }

            if (target instanceof HttpUrlGetter) {
                final HttpUrl url = ((HttpUrlGetter) target)._$PINPOINT$_getHttpUrl();
                if (url != null) {
                    final String endpoint = getDestinationId(url);
                    builder.header(Header.HTTP_HOST.toString(), endpoint);
                    if (isDebug) {
                        logger.debug("Set HTTP header. host={}", endpoint);
                    }
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

    private String getDestinationId(HttpUrl httpUrl) {
        if (httpUrl == null || httpUrl.host() == null) {
            return "UnknownHttpClient";
        }
        final int port = EndPointUtils.getPort(httpUrl.port(), HttpUrl.defaultPort(httpUrl.scheme()));
        return HostAndPort.toHostAndPortString(httpUrl.host(), port);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }
    }
}