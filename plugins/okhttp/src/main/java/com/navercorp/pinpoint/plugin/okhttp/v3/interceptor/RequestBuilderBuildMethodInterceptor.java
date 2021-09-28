/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.okhttp.v3.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientHeaderAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.DefaultRequestTraceWriter;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceWriter;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.plugin.okhttp.EndPointUtils;
import com.navercorp.pinpoint.plugin.okhttp.v3.HttpUrlGetter;
import com.navercorp.pinpoint.plugin.okhttp.v3.RequestBuilder3ClientHeaderAdaptor;
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
    private final RequestTraceWriter<Request.Builder> requestTraceWriter;;

    public RequestBuilderBuildMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, InterceptorScope interceptorScope) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
        this.interceptorScope = interceptorScope;

        ClientHeaderAdaptor<Request.Builder> clientHeaderAdaptor = new RequestBuilder3ClientHeaderAdaptor();
        this.requestTraceWriter = new DefaultRequestTraceWriter<>(clientHeaderAdaptor, traceContext);
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
                this.requestTraceWriter.write(builder);
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
            final String host = getHost(target);
            this.requestTraceWriter.write(builder, nextId, host);
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

    private String getHost(Object target) {
        if (target instanceof HttpUrlGetter) {
            final HttpUrl url = ((HttpUrlGetter) target)._$PINPOINT$_getHttpUrl();
            if (url != null) {
                return getDestinationId(url);
            }
        }
        return null;
    }

    private String getDestinationId(HttpUrl httpUrl) {
        if (httpUrl == null || httpUrl.host() == null) {
            return "Unknown";
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