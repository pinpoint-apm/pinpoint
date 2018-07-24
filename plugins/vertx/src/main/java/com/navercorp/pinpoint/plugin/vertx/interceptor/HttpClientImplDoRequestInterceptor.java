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
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientHeaderAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.DefaultRequestTraceWriter;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceWriter;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.vertx.HttpClientRequestClientHeaderAdaptor;
import com.navercorp.pinpoint.plugin.vertx.VertxConstants;
import io.vertx.core.http.HttpClientRequest;

/**
 * @author jaehong.kim
 */
public class HttpClientImplDoRequestInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;
    private final RequestTraceWriter<HttpClientRequest> requestTraceWriter;

    public HttpClientImplDoRequestInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.methodDescriptor = descriptor;

        ClientHeaderAdaptor<HttpClientRequest> clientHeaderAdaptor = new HttpClientRequestClientHeaderAdaptor();
        this.requestTraceWriter = new DefaultRequestTraceWriter<HttpClientRequest>(clientHeaderAdaptor, traceContext);
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

        if (!trace.canSampled()) {
            return;
        }

        trace.traceBlockBegin();
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        HttpClientRequest resultToRequest = null;
        if (validate(result)) {
            resultToRequest = (HttpClientRequest) result;
        }
        final HttpClientRequest request = resultToRequest;

        if (!trace.canSampled()) {
            if (request != null) {
                requestTraceWriter.write(request);
            }
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(methodDescriptor);
            recorder.recordException(throwable);
            recorder.recordServiceType(VertxConstants.VERTX_HTTP_CLIENT_INTERNAL);

            final String hostAndPort = toHostAndPort(args);
            if (hostAndPort != null) {
                recorder.recordAttribute(AnnotationKey.HTTP_INTERNAL_DISPLAY, hostAndPort);
                if (isDebug) {
                    logger.debug("Set hostAndPort {}", hostAndPort);
                }
            }

            if (request != null) {
                // make asynchronous trace-id
                final AsyncContext asyncContext = recorder.recordNextAsyncContext();
                ((AsyncContextAccessor) request)._$PINPOINT$_setAsyncContext(asyncContext);
                if (isDebug) {
                    logger.debug("Set asyncContext {}", asyncContext);
                }
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    private boolean validate(final Object result) {
        if (result == null || !(result instanceof HttpClientRequest)) {
            if (isDebug) {
                logger.debug("Invalid result object. result={}.", result);
            }
            return false;
        }

        if (!(result instanceof AsyncContextAccessor)) {
            if (isDebug) {
                logger.debug("Invalid result object. Need metadata accessor({}).", AsyncContextAccessor.class.getName());
            }
            return false;
        }

        return true;
    }

    private String toHostAndPort(final Object[] args) {
        if (args != null && (args.length == 5 || args.length == 6)) {
            if (args[1] instanceof String && args[2] instanceof Integer) {
                final String host = (String) args[1];
                final int port = (Integer) args[2];
                return HostAndPort.toHostAndPortString(host, port);
            }
        } else if (args != null && args.length == 7) {
            // 3.4.2 - HttpMethod method, String host, int port, Boolean ssl, String relativeURI, MultiMap headers
            if (args[2] instanceof String && args[3] instanceof Integer) {
                final String host = (String) args[2];
                final int port = (Integer) args[3];
                return HostAndPort.toHostAndPortString(host, port);
            }
        }

        if (isDebug) {
            logger.debug("Invalid args[]. args={}.", args);
        }
        return null;
    }
}
