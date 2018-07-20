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
package com.navercorp.pinpoint.plugin.ning.asynchttpclient.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceWriter;
import com.navercorp.pinpoint.plugin.ning.asynchttpclient.NingAsyncHttpClientConstants;
import com.navercorp.pinpoint.plugin.ning.asynchttpclient.NingAsyncHttpClientPluginConfig;
import com.navercorp.pinpoint.plugin.ning.asynchttpclient.NingAsyncHttpClientRequestWrapperV2;
import org.asynchttpclient.Request;

/**
 * @author jaehong.kim
 */
public class ExecuteInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;

    private final ClientRequestRecorder clientRequestRecorder;

    public ExecuteInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        final NingAsyncHttpClientPluginConfig config = new NingAsyncHttpClientPluginConfig(traceContext.getProfilerConfig());
        this.clientRequestRecorder = new ClientRequestRecorder(config.isParam(), config.getHttpDumpConfig());
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

        if (!validate(args)) {
            return;
        }

        final Request httpRequest = (Request) args[0];
        final boolean sampling = trace.canSampled();
        if (!sampling) {
            if (httpRequest != null) {
                final RequestTraceWriter requestTraceWriter = new RequestTraceWriter(new NingAsyncHttpClientRequestWrapperV2(httpRequest));
                requestTraceWriter.write();
            }
            return;
        }

        trace.traceBlockBegin();
        final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
        final TraceId nextId = trace.getTraceId().getNextTraceId();
        recorder.recordNextSpanId(nextId.getSpanId());
        recorder.recordServiceType(NingAsyncHttpClientConstants.ASYNC_HTTP_CLIENT);
        if (httpRequest != null) {
            final RequestTraceWriter requestTraceWriter = new RequestTraceWriter(new NingAsyncHttpClientRequestWrapperV2(httpRequest));
            requestTraceWriter.write(nextId, this.traceContext.getApplicationName(), this.traceContext.getServerTypeCode(), this.traceContext.getProfilerConfig().getApplicationNamespace());
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            // Do not log result
            logger.afterInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        if (!validate(args)) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            final Request httpRequest = (Request) args[0];
            if (httpRequest != null) {
                // Accessing httpRequest here not BEFORE() because it can cause side effect.
                this.clientRequestRecorder.record(recorder, new NingAsyncHttpClientRequestWrapperV2(httpRequest), throwable);
            }
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
        } finally {
            trace.traceBlockEnd();
        }
    }

    private boolean validate(final Object[] args) {
        if (args == null || args.length == 0 || !(args[0] instanceof Request)) {
            if (isDebug) {
                logger.debug("Invalid args[0] object. args={}.", args);
            }
            return false;
        }

        return true;
    }
}