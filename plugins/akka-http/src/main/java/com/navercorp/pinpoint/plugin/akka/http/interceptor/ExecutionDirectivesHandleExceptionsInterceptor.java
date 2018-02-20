/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.akka.http.interceptor;

import akka.http.javadsl.model.HttpRequest;
import akka.http.scaladsl.server.RequestContextImpl;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.proxy.ProxyHttpHeaderHandler;
import com.navercorp.pinpoint.bootstrap.plugin.proxy.ProxyHttpHeaderRecorder;
import com.navercorp.pinpoint.plugin.akka.http.AkkaHttpConstants;

import java.util.Optional;

public class ExecutionDirectivesHandleExceptionsInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(ExecutionDirectivesHandleExceptionsInterceptor.class);
    private final boolean isDebug = logger.isDebugEnabled();
    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private static final AkkaHttpServerMethodDescriptor AKKA_HTTP_SERVER_METHOD_DESCRIPTOR = new AkkaHttpServerMethodDescriptor();
    private final ProxyHttpHeaderRecorder proxyHttpHeaderRecorder;

    public ExecutionDirectivesHandleExceptionsInterceptor(final TraceContext traceContext, final MethodDescriptor methodDescriptor) {
        this.traceContext = traceContext;
        this.descriptor = methodDescriptor;
        this.proxyHttpHeaderRecorder = new ProxyHttpHeaderRecorder(traceContext.getProfilerConfig().isProxyHttpHeaderEnable());
    }

    @Override
    public void before(Object target, Object[] args) {
        if (traceContext.currentTraceObject() == null) {
            for (Object param : args) {
                if (param instanceof RequestContextImpl) {
                    final RequestContextImpl request = (RequestContextImpl) param;
                    final Trace trace = createTrace(request.request());
                    if (!trace.canSampled()) {
                        return;
                    }
                    final SpanEventRecorder recorder = trace.traceBlockBegin();
                    recorder.recordServiceType(AkkaHttpConstants.AKKA_HTTP_SERVER_INTERNAL);
                    final AsyncContext asyncContext = recorder.recordNextAsyncContext(true);
                    ((AsyncContextAccessor) request)._$PINPOINT$_setAsyncContext(asyncContext);
                    if (isDebug) {
                        logger.debug("Set closeable-AsyncContext {}", asyncContext);
                    }
                    break;
                }
            }
        }
    }

    private Trace createTrace(final HttpRequest request) {
        final Trace trace = traceContext.newAsyncTraceObject();
        if (trace.canSampled()) {
            final SpanRecorder recorder = trace.getSpanRecorder();
            recordRootSpan(recorder, request);
        } else {
            if (isDebug) {
                logger.debug("Sampling is disabled");
            }
        }
        return trace;
    }

    private void recordRootSpan(final SpanRecorder recorder, final HttpRequest request) {
        recorder.recordServiceType(AkkaHttpConstants.AKKA_HTTP);
        final String requestURL = request.getUri().toString();
        recorder.recordRpcName(requestURL);
        recorder.recordApi(AKKA_HTTP_SERVER_METHOD_DESCRIPTOR);
        this.proxyHttpHeaderRecorder.record(recorder, new ProxyHttpHeaderHandler() {
            @Override
            public String read(String name) {
                return request.getHeader(name).flatMap(httpHeader -> Optional.of(httpHeader.value())).orElse(null);
            }
        });
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        if (!trace.canSampled()) {
            traceContext.removeTraceObject();
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", t.getMessage(), t);
            }
        } finally {
            trace.traceBlockEnd();
            deleteTrace(trace);
        }
    }

    private void deleteTrace(final Trace trace) {
        traceContext.removeTraceObject();
        trace.close();
    }
}
