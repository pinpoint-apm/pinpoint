/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.webflux.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.ResultReplaceAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.plugin.reactorsupport.SeamPublisherWrapper;
import com.navercorp.pinpoint.plugin.spring.webflux.SpringWebFluxConstants;

import java.util.Objects;

/**
 * Wrapping variant of {@link DispatchHandlerHandleMethodInterceptor} (config-gated by
 * {@code profiler.spring.webflux.wrap.publisher}). The exchange (args[0]) still receives the
 * AsyncContext through its injected accessor field - downstream interceptors read it from
 * there - but the returned publisher is replaced with a wrapped one instead of being injected.
 */
public class WrappingDispatchHandlerHandleMethodInterceptor implements ResultReplaceAroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;

    public WrappingDispatchHandlerHandleMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        this.traceContext = Objects.requireNonNull(traceContext, "traceContext");
        this.methodDescriptor = Objects.requireNonNull(methodDescriptor, "methodDescriptor");
    }

    @Override
    public void before(Object target, Class<?> returnType, Object[] args) {
        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(SpringWebFluxConstants.SPRING_WEBFLUX);
            if (args != null && args.length > 0 && args[0] instanceof AsyncContextAccessor) {
                // make asynchronous trace-id
                final AsyncContext asyncContext = recorder.recordNextAsyncContext();
                ((AsyncContextAccessor) args[0])._$PINPOINT$_setAsyncContext(asyncContext);
                if (isDebug) {
                    logger.debug("Set AsyncContext {}", asyncContext);
                }
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    @Override
    public Object after(Object target, Class<?> returnType, Object[] args, Object result, Throwable throwable) {
        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return result;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(methodDescriptor);
            recorder.recordException(throwable);

            if (throwable != null || !SeamPublisherWrapper.isWrappable(result)) {
                return result;
            }

            final AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(args, 0);
            if (asyncContext == null) {
                return result;
            }
            final Object wrapped = SeamPublisherWrapper.wrap(result, asyncContext);
            if (isDebug) {
                logger.debug("Wrapped result publisher. asyncContext={}", asyncContext);
            }
            return wrapped;
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
            return result;
        } finally {
            trace.traceBlockEnd();
        }
    }
}
