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
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.request.ApplicationInfoSender;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientHeaderAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestAdaptor;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.DefaultApplicationInfoSender;
import com.navercorp.pinpoint.bootstrap.plugin.request.DefaultRequestTraceWriter;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceWriter;
import com.navercorp.pinpoint.plugin.jdk.http.HttpURLConnectionClientHeaderAdaptor;
import com.navercorp.pinpoint.plugin.jdk.http.JdkHttpClientRequestAdaptor;
import com.navercorp.pinpoint.plugin.jdk.http.JdkHttpConstants;
import com.navercorp.pinpoint.plugin.jdk.http.JdkHttpPluginConfig;

import java.net.HttpURLConnection;

/**
 * @author netspider
 * @author emeroad
 * @author yjqg6666
 */
public abstract class AbstractHttpURLConnectionInterceptor implements AroundInterceptor {
    private static final Object TRACE_BLOCK_BEGIN_MARKER = new Object();
    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final InterceptorScope scope;

    private final ClientRequestRecorder<HttpURLConnection> clientRequestRecorder;
    private final RequestTraceWriter<HttpURLConnection> requestTraceWriter;
    private final ClientRequestAdaptor<HttpURLConnection> clientRequestAdaptor = new JdkHttpClientRequestAdaptor();
    private final ApplicationInfoSender<HttpURLConnection> applicationInfoSender;

    public AbstractHttpURLConnectionInterceptor(TraceContext traceContext, MethodDescriptor descriptor, InterceptorScope scope) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.scope = scope;

        final JdkHttpPluginConfig config = new JdkHttpPluginConfig(traceContext.getProfilerConfig());
        this.clientRequestRecorder = new ClientRequestRecorder<>(config.isParam(), clientRequestAdaptor);
        final ClientHeaderAdaptor<HttpURLConnection> clientHeaderAdaptor = new HttpURLConnectionClientHeaderAdaptor();
        this.requestTraceWriter = new DefaultRequestTraceWriter<>(clientHeaderAdaptor, traceContext);
        this.applicationInfoSender = new DefaultApplicationInfoSender<>(clientHeaderAdaptor, traceContext);
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final HttpURLConnection request = (HttpURLConnection) target;
            final boolean canSample = trace.canSampled();
            if (canSample) {
                final TraceId nextId = trace.getTraceId().getNextTraceId();
                String host = this.clientRequestAdaptor.getDestinationId(request);
                try {
                    this.requestTraceWriter.write(request, nextId, host);
                } catch (Exception e) {
                    // It happens if it is already connected or connected.
                    if(isDebug) {
                        logger.debug("Failed to requestTraceWriter, already connected or connected");
                    }
                    return;
                }
                scope.getCurrentInvocation().setAttachment(TRACE_BLOCK_BEGIN_MARKER);
                final SpanEventRecorder recorder = trace.traceBlockBegin();
                recorder.recordServiceType(JdkHttpConstants.SERVICE_TYPE);
                recorder.recordNextSpanId(nextId.getSpanId());
            } else {
                try {
                    this.requestTraceWriter.write(request);
                } catch (Exception ignored) {
                    // It happens if it is already connected or connected.
                    if(isDebug) {
                        logger.debug("Failed to requestTraceWriter, already connected or connected");
                    }
                }
            }
            this.applicationInfoSender.sendCallerApplicationName(request);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            // do not log result
            logger.afterInterceptor(target, args);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        final InterceptorScopeInvocation currentInvocation = scope.getCurrentInvocation();
        if (TRACE_BLOCK_BEGIN_MARKER != currentInvocation.getAttachment()) {
            return;
        }

        try {
            final HttpURLConnection request = (HttpURLConnection) target;
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
            this.clientRequestRecorder.record(recorder, request, throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        } finally {
            currentInvocation.removeAttachment();
            trace.traceBlockEnd();
        }
    }
}
