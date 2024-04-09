/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.vertx.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.ApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.common.plugin.util.HostAndPort;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.vertx.VertxConstants;

public class HttpClientImplDoRequestInterceptorV4 implements ApiIdAwareAroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;

    public HttpClientImplDoRequestInterceptorV4(TraceContext traceContext) {
        this.traceContext = traceContext;
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
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
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

            if (target instanceof AsyncContextAccessor) {
                // make asynchronous trace-id
                final AsyncContext asyncContext = recorder.recordNextAsyncContext();
                ((AsyncContextAccessor) target)._$PINPOINT$_setAsyncContext(asyncContext);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    private String toHostAndPort(final Object[] args) {
        final int length = ArrayUtils.getLength(args);
        if (length == 12) {
            if (args[3] instanceof String && args[4] instanceof Integer) {
                final String host = (String) args[3];
                final int port = (Integer) args[4];
                return HostAndPort.toHostAndPortString(host, port);
            }
        } else if (length == 13) {
            if (args[3] instanceof String && args[4] instanceof Integer) {
                final String host = (String) args[3];
                final int port = (Integer) args[4];
                return HostAndPort.toHostAndPortString(host, port);
            }
        } else if (length == 14) {
            if (args[2] instanceof String && args[3] instanceof Integer) {
                final String host = (String) args[2];
                final int port = (Integer) args[3];
                return HostAndPort.toHostAndPortString(host, port);
            }
        }

        return null;
    }
}