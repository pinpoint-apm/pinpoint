/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jdk.http.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.response.ResponseHeaderRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.response.ServerResponseHeaderRecorder;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.jdk.http.JdkHttpClientResponseAdaptor;
import com.navercorp.pinpoint.plugin.jdk.http.JdkHttpConstants;

import java.net.HttpURLConnection;

public class HttpURLConnectionGetHeaderFieldInterceptor implements AroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(getClass());

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;
    private final ServerResponseHeaderRecorder<HttpURLConnection> responseHeaderRecorder;

    public HttpURLConnectionGetHeaderFieldInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.methodDescriptor = descriptor;
        this.responseHeaderRecorder = ResponseHeaderRecorderFactory.newResponseHeaderRecorder(traceContext.getProfilerConfig(), new JdkHttpClientResponseAdaptor());
    }

    @Override
    public void before(Object target, Object[] args) {
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        if (Boolean.FALSE == isInTraceScope(trace)) {
            return;
        }

        if (Boolean.FALSE == isResponseCode(args)) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(JdkHttpConstants.SERVICE_TYPE_INTERNAL);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        if (Boolean.FALSE == isInTraceScope(trace)) {
            return;
        }

        if (Boolean.FALSE == isResponseCode(args)) {
            return;
        }

        try {
            final HttpURLConnection request = (HttpURLConnection) target;
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(methodDescriptor);
            recorder.recordException(throwable);

            if (result instanceof String) {
                final String response = (String) result;
                if (response.startsWith("HTTP/1.")) {
                    int statusCode = toStatusCode(response);
                    if (statusCode > 0) {
                        recorder.recordAttribute(AnnotationKey.HTTP_STATUS_CODE, statusCode);
                    }
                    this.responseHeaderRecorder.recordHeader(recorder, request);
                }
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    private boolean isResponseCode(Object[] args) {
        Integer index = ArrayArgumentUtils.getArgument(args, 0, Integer.class);
        if (index == null || index != 0) {
            return false;
        }

        return true;
    }

    private boolean isInTraceScope(Trace trace) {
        final TraceScope scope = trace.getScope(JdkHttpConstants.TRACE_SCOPE_NAME_GET_INPUT_STREAM);
        if (scope != null) {
            return scope.isActive();
        }
        return false;
    }

    private int toStatusCode(String statusLine) {
        int index = statusLine.indexOf(' ');
        if (index > 0) {
            int position = statusLine.indexOf(' ', index + 1);
            if (position < 0)
                position = statusLine.length();

            try {
                return Integer.parseInt(statusLine.substring(index + 1, position));
            } catch (NumberFormatException ignored) {
            }
        }
        return -1;
    }
}