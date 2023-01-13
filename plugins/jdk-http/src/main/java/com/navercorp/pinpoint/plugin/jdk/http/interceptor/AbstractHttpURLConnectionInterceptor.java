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

package com.navercorp.pinpoint.plugin.jdk.http.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientHeaderAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.DefaultRequestTraceWriter;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceWriter;
import com.navercorp.pinpoint.bootstrap.plugin.response.ResponseHeaderRecorderFactory;
import com.navercorp.pinpoint.bootstrap.plugin.response.ServerResponseHeaderRecorder;
import com.navercorp.pinpoint.bootstrap.util.ScopeUtils;
import com.navercorp.pinpoint.plugin.jdk.http.ConnectingGetter;
import com.navercorp.pinpoint.plugin.jdk.http.HttpURLConnectionClientHeaderAdaptor;
import com.navercorp.pinpoint.plugin.jdk.http.JdkHttpClientRequestAdaptor;
import com.navercorp.pinpoint.plugin.jdk.http.JdkHttpClientResponseAdaptor;
import com.navercorp.pinpoint.plugin.jdk.http.JdkHttpConstants;
import com.navercorp.pinpoint.plugin.jdk.http.JdkHttpPlugin;
import com.navercorp.pinpoint.plugin.jdk.http.JdkHttpPluginConfig;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author netspider
 * @author emeroad
 * @author yjqg6666
 */
public abstract class AbstractHttpURLConnectionInterceptor implements AroundInterceptor {
    private static final Object TRACE_BLOCK_BEGIN_MARKER = new Object();
    private static final String TRACE_SCOPE_NAME_RESPONSE = "_JdkHttpInputStreamRespRecording";
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final InterceptorScope scope;
    private final ClientRequestRecorder<HttpURLConnection> clientRequestRecorder;
    private final ServerResponseHeaderRecorder<HttpURLConnection> responseHeaderRecorder;

    private final RequestTraceWriter<HttpURLConnection> requestTraceWriter;

    public AbstractHttpURLConnectionInterceptor(TraceContext traceContext, MethodDescriptor descriptor, InterceptorScope scope) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.scope = scope;

        final JdkHttpPluginConfig config = new JdkHttpPluginConfig(traceContext.getProfilerConfig());

        ClientRequestAdaptor<HttpURLConnection> clientRequestAdaptor = new JdkHttpClientRequestAdaptor();
        this.clientRequestRecorder = new ClientRequestRecorder<>(config.isParam(), clientRequestAdaptor);
        this.responseHeaderRecorder = ResponseHeaderRecorderFactory.newResponseHeaderRecorder(traceContext.getProfilerConfig(), new JdkHttpClientResponseAdaptor());

        ClientHeaderAdaptor<HttpURLConnection> clientHeaderAdaptor = new HttpURLConnectionClientHeaderAdaptor();
        this.requestTraceWriter = new DefaultRequestTraceWriter<>(clientHeaderAdaptor, traceContext);
    }

    abstract boolean isConnected(Object target);

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (target == null) {
            return;
        }

        Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        final boolean interceptingGetInputStream = isInterceptingGetInputStream();
        final boolean recordingResponse = isRecordingResponse(trace);
        if (recordingResponse) {
            if (interceptingGetInputStream) {
                return;
            } else {
                clearRecordingResponseStatus(trace);
            }
        }

        boolean connected = isConnected(target);
        boolean connecting = false;
        if (target instanceof ConnectingGetter) {
            connecting = ((ConnectingGetter) target)._$PINPOINT$_isConnecting();
        }

        boolean addRequestHeader = !connected && !connecting;
        if (isInterceptingHttps()) {
            addRequestHeader = addRequestHeader && isInterceptingConnect();
        }

        final HttpURLConnection request = (HttpURLConnection) target;
        final boolean canSample = trace.canSampled();
        if (canSample) {
            scope.getCurrentInvocation().setAttachment(TRACE_BLOCK_BEGIN_MARKER);
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(JdkHttpConstants.SERVICE_TYPE);
            if (addRequestHeader) {
                final TraceId nextId = trace.getTraceId().getNextTraceId();
                recorder.recordNextSpanId(nextId.getSpanId());
                String host = getHost(request);
                this.requestTraceWriter.write(request, nextId, host);
            }
        } else {
            if (addRequestHeader) {
                this.requestTraceWriter.write(request);
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            // do not log result
            logger.afterInterceptor(target, args);
        }
        if (target == null) {
            return;
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null || !trace.canSampled()) {
            return;
        }

        final InterceptorScopeInvocation currentInvocation = scope.getCurrentInvocation();
        if (TRACE_BLOCK_BEGIN_MARKER != currentInvocation.getAttachment()) {
            return;
        }

        final boolean interceptingGetInputStream = isInterceptingGetInputStream();
        if (interceptingGetInputStream && isRecordingResponse(trace)) {
            return;
        }

        try {
            final HttpURLConnection request = (HttpURLConnection) target;
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
            this.clientRequestRecorder.record(recorder, request, throwable);
            if (interceptingGetInputStream) {
                if (startRecordingResponse(trace)) {
                    this.responseHeaderRecorder.recordHeader(recorder, request);
                }
            }
        } finally {
            currentInvocation.removeAttachment();
            trace.traceBlockEnd();
        }
    }

    private String getHost(HttpURLConnection httpURLConnection) {
        final URL url = httpURLConnection.getURL();
        if (url != null) {
            final String host = url.getHost();
            final int port = url.getPort();
            if (host != null) {
                return JdkHttpClientRequestAdaptor.getEndpoint(host, port);
            }
        }
        return null;
    }

    private boolean isInterceptingGetInputStream() {
        return "getInputStream".contentEquals(this.descriptor.getMethodName());
    }

    private boolean isInterceptingConnect() {
        return "connect".contentEquals(this.descriptor.getMethodName());
    }

    private boolean isInterceptingHttps() {
        return JdkHttpPlugin.INTERCEPT_HTTPS_CLASS_NAME.contentEquals(this.descriptor.getClassName());
    }

    private boolean isRecordingResponse(Trace trace) {
        return ScopeUtils.isEndScope(trace, TRACE_SCOPE_NAME_RESPONSE);
    }

    private boolean startRecordingResponse(Trace trace) {
        TraceScope scope = trace.getScope(TRACE_SCOPE_NAME_RESPONSE);
        if (scope == null) {
            trace.addScope(TRACE_SCOPE_NAME_RESPONSE);
            scope = trace.getScope(TRACE_SCOPE_NAME_RESPONSE);
        }
        if (scope != null) {
            final boolean ok = scope.tryEnter();
            if (!ok) {
                logger.warn("Try to startRecording response failed, tryEnter scope failed");
            }
            return ok;
        } else {
            logger.warn("Try to startRecording response failed, getOrAdd scope failed");
            return false;
        }
    }

    private void clearRecordingResponseStatus(Trace trace) {
        if (!ScopeUtils.leaveScope(trace, TRACE_SCOPE_NAME_RESPONSE)) {
            logger.warn("Try to  learRecordingResponseStatus failed, canLeave scope returned false");
        }
    }

}
