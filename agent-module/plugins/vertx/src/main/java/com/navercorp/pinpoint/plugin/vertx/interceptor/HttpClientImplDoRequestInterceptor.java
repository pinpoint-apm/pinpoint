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
package com.navercorp.pinpoint.plugin.vertx.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.ApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientHeaderAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.DefaultRequestTraceWriter;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceWriter;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.vertx.HttpClientRequestClientHeaderAdaptor;
import com.navercorp.pinpoint.plugin.vertx.VertxConstants;
import io.vertx.core.http.HttpClientRequest;

/**
 * @author jaehong.kim
 */
public class HttpClientImplDoRequestInterceptor implements ApiIdAwareAroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final RequestTraceWriter<HttpClientRequest> requestTraceWriter;

    public HttpClientImplDoRequestInterceptor(TraceContext traceContext) {
        this.traceContext = traceContext;
        ClientHeaderAdaptor<HttpClientRequest> clientHeaderAdaptor = new HttpClientRequestClientHeaderAdaptor();
        this.requestTraceWriter = new DefaultRequestTraceWriter<>(clientHeaderAdaptor, traceContext);
    }

    @Override
    public void before(Object target, int apiId, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        trace.traceBlockBegin();
    }

    @Override
    public void after(Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        try {
            HttpClientRequest resultToRequest = null;
            if (validate(result)) {
                resultToRequest = (HttpClientRequest) result;
            }
            final HttpClientRequest request = resultToRequest;
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();

            if (request != null) {
                requestTraceWriter.write(request, trace.getRequestId());
            }
            if (trace.canSampled()) {
                recorder.recordApiId(apiId);
                recorder.recordException(throwable);
                recorder.recordServiceType(VertxConstants.VERTX_HTTP_CLIENT_INTERNAL);
                final String hostAndPort = toHostAndPort(args);
                if (hostAndPort != null) {
                    recorder.recordAttribute(AnnotationKey.HTTP_INTERNAL_DISPLAY, hostAndPort);
                    if (isDebug) {
                        logger.debug("Set hostAndPort {}", hostAndPort);
                    }
                }
            }

            if (request != null) {
                // make asynchronous trace-id
                final AsyncContext asyncContext = recorder.recordNextAsyncContext();
                ((AsyncContextAccessor) request)._$PINPOINT$_setAsyncContext(asyncContext);
                if (isDebug) {
                    logger.debug("Set asyncContext to request. asyncContext={}", asyncContext);
                }
            }
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", t.getMessage(), t);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    private boolean validate(final Object result) {
        if (result == null || !(result instanceof HttpClientRequest)) {
            return false;
        }

        if (!(result instanceof AsyncContextAccessor)) {
            return false;
        }

        return true;
    }

    private String toHostAndPort(final Object[] args) {
        final int length = ArrayUtils.getLength(args);
        if (length == 5 || length == 6) {
            if (args[1] instanceof String && args[2] instanceof Integer) {
                final String host = (String) args[1];
                final int port = (Integer) args[2];
                return HostAndPort.toHostAndPortString(host, port);
            }
        } else if (length == 7) {
            // 3.4.2 - HttpMethod method, String host, int port, Boolean ssl, String relativeURI, MultiMap headers
            if (args[2] instanceof String && args[3] instanceof Integer) {
                final String host = (String) args[2];
                final int port = (Integer) args[3];
                return HostAndPort.toHostAndPortString(host, port);
            }
        } else if (length == 8) {
            if (args[3] instanceof String && args[4] instanceof Integer) {
                final String host = (String) args[3];
                final int port = (Integer) args[4];
                return HostAndPort.toHostAndPortString(host, port);
            }
        }

        return null;
    }
}
