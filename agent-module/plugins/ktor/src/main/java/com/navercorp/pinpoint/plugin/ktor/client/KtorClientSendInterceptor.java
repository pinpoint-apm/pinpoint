/*
 * Copyright 2026 NAVER Corp.
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


package com.navercorp.pinpoint.plugin.ktor.client;

import com.navercorp.pinpoint.plugin.ktor.KtorConstants;
import com.navercorp.pinpoint.plugin.ktor.KtorPluginConfig;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.plugin.request.ClientRequestRecorder;
import com.navercorp.pinpoint.bootstrap.plugin.request.DefaultRequestTraceWriter;
import com.navercorp.pinpoint.bootstrap.plugin.request.RequestTraceWriter;

public class KtorClientSendInterceptor implements AroundInterceptor {
    private static final String HTTP_REQUEST_BUILDER_CLASS_NAME = "io.ktor.client.request.HttpRequestBuilder";

    private final PluginLogger logger = PluginLogManager.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;
    private final KtorPluginConfig config;
    private final KtorClientRequestAdaptor requestAdaptor;
    private final ClientRequestRecorder<Object> requestRecorder;
    private final RequestTraceWriter<Object> traceWriter;
    private final ThreadLocal<KtorClientParentTraceState> activeState = new ThreadLocal<>();

    public KtorClientSendInterceptor(
            TraceContext traceContext,
            MethodDescriptor methodDescriptor
    ) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
        this.config = new KtorPluginConfig(traceContext.getProfilerConfig());
        this.requestAdaptor = new KtorClientRequestAdaptor();
        this.requestRecorder = new ClientRequestRecorder<>(this.config.isClientParam(), requestAdaptor);
        this.traceWriter = new DefaultRequestTraceWriter<>(new KtorClientHeaderAdaptor(), traceContext);
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        activeState.remove();
        KtorClientTraceStorage.clearPending();

        Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        Object request = getRequest(args);
        if (request == null) {
            return;
        }

        SpanEventRecorder recorder = null;
        boolean blockStarted = false;
        try {
            String destinationId = requestAdaptor.getDestinationId(request);
            if (!trace.canSampled()) {
                traceWriter.write(request);
                return;
            }

            recorder = trace.traceBlockBegin();
            blockStarted = true;
            TraceId nextId = trace.getTraceId().getNextTraceId();
            recorder.recordNextSpanId(nextId.getSpanId());
            recorder.recordServiceType(KtorConstants.KTOR_CLIENT);
            traceWriter.write(request, nextId, destinationId);
            AsyncContext asyncContext = recorder.recordNextAsyncContext(true);

            KtorClientTraceHolder holder = new KtorClientTraceHolder(
                    asyncContext,
                    request,
                    methodDescriptor,
                    config,
                    requestRecorder
            );
            activeState.set(new KtorClientParentTraceState(trace, holder));
            KtorClientTraceStorage.setPending(holder);
        } catch (Throwable throwable) {
            logger.warn("Failed to write Ktor client trace headers. {}", throwable.getMessage(), throwable);
            if (blockStarted) {
                try {
                    trace.traceBlockEnd();
                } catch (Throwable closeThrowable) {
                    logger.warn(
                            "Failed to close Ktor client trace block after header write failure. {}",
                            closeThrowable.getMessage(),
                            closeThrowable
                    );
                }
            }
            activeState.remove();
            KtorClientTraceStorage.clearPending();
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }

        KtorClientParentTraceState state = activeState.get();
        activeState.remove();
        KtorClientTraceStorage.clearPending();
        if (state == null) {
            // The suspended continuation may finish the async trace on a different coroutine thread.
            return;
        }

        KtorClientTraceHolder holder = state.getHolder();
        if (KtorClientCoroutineSuspendedMarker.isSuspended(result) && holder.isAttached()) {
            state.finish(null);
            return;
        }

        try {
            state.finish(throwable);
            holder.cancelAsync();
        } catch (Throwable finishThrowable) {
            logger.warn("Failed to finish Ktor client trace in send interceptor. {}", finishThrowable.getMessage(), finishThrowable);
        }
    }

    private Object getRequest(Object[] args) {
        if (args == null || args.length == 0 || !isHttpRequestBuilder(args[0])) {
            return null;
        }
        return args[0];
    }

    private boolean isHttpRequestBuilder(Object request) {
        if (request == null) {
            return false;
        }

        Class<?> type = request.getClass();
        while (type != null) {
            if (HTTP_REQUEST_BUILDER_CLASS_NAME.equals(type.getName())) {
                return true;
            }
            type = type.getSuperclass();
        }
        return false;
    }
}
