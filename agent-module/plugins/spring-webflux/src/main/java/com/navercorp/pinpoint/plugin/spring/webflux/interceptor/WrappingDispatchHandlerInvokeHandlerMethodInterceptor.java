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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.ResultReplaceAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;
import com.navercorp.pinpoint.bootstrap.util.ScopeUtils;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.reactorsupport.SeamPublisherWrapper;
import com.navercorp.pinpoint.plugin.spring.webflux.SpringWebFluxConstants;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.util.pattern.PathPattern;

import java.util.Objects;

/**
 * Wrapping variant of {@link DispatchHandlerInvokeHandlerMethodInterceptor} (config-gated by
 * {@code profiler.spring.webflux.wrap.publisher}). The span event still runs inside the async
 * trace continued from the exchange's AsyncContext, but the returned publisher is replaced with
 * a wrapped one (carrying the same context) instead of being injected.
 */
public class WrappingDispatchHandlerInvokeHandlerMethodInterceptor implements ResultReplaceAroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;
    private final Boolean uriStatEnable;
    private final Boolean uriStatUseUserInput;

    public WrappingDispatchHandlerInvokeHandlerMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, Boolean uriStatEnable, Boolean uriStatUseUserInput) {
        this.traceContext = Objects.requireNonNull(traceContext, "traceContext");
        this.methodDescriptor = Objects.requireNonNull(methodDescriptor, "methodDescriptor");
        this.uriStatEnable = uriStatEnable;
        this.uriStatUseUserInput = uriStatUseUserInput;
    }

    @Override
    public void before(Object target, Class<?> returnType, Object[] args) {
        final AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(args, 0);
        if (asyncContext == null) {
            return;
        }
        final Trace trace = asyncContext.continueAsyncTraceObject(true);
        if (trace == null) {
            return;
        }

        ScopeUtils.entryAsyncTraceScope(trace);
        try {
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(SpringWebFluxConstants.SPRING_WEBFLUX);
            if (uriStatEnable) {
                recordUriTemplate(args);
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    // same recording as DispatchHandlerInvokeHandlerMethodInterceptor.doInBeforeTrace
    private void recordUriTemplate(Object[] args) {
        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        final ServerWebExchange exchange = ArrayArgumentUtils.getArgument(args, 0, ServerWebExchange.class);
        if (exchange != null) {
            String uriTemplate = "";

            if (uriStatUseUserInput) {
                for (String attributeName : SpringWebFluxConstants.SPRING_WEBFLUX_URI_USER_INPUT_ATTRIBUTE_KEYS) {
                    final Object uriMapping = exchange.getAttribute(attributeName);
                    if (!(uriMapping instanceof String)) {
                        continue;
                    }
                    uriTemplate = (String) uriMapping;
                }
            }
            if (!StringUtils.hasLength(uriTemplate)) {
                for (String attributeName : SpringWebFluxConstants.SPRING_WEBFLUX_DEFAULT_URI_ATTRIBUTE_KEYS) {
                    final Object uriMapping = exchange.getAttribute(attributeName);
                    if (!(uriMapping instanceof PathPattern)) {
                        continue;
                    }
                    uriTemplate = ((PathPattern) uriMapping).getPatternString();
                }
            }

            if (StringUtils.hasLength(uriTemplate)) {
                final SpanRecorder spanRecorder = trace.getSpanRecorder();
                spanRecorder.recordUriTemplate(uriTemplate, true);
            }
        }
    }

    @Override
    public Object after(Object target, Class<?> returnType, Object[] args, Object result, Throwable throwable) {
        final AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(args, 0);
        if (asyncContext == null) {
            return result;
        }
        final Trace trace = asyncContext.currentAsyncTraceObject();
        if (trace == null) {
            return result;
        }

        if (!ScopeUtils.leaveAsyncTraceScope(trace)) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to leave scope of async trace {}.", trace);
            }
            deleteAsyncContext(trace, asyncContext);
            return result;
        }

        Object ret = result;
        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(methodDescriptor);
            recorder.recordException(throwable);

            if (throwable == null && SeamPublisherWrapper.isWrappable(result)) {
                // hand the exchange's AsyncContext to the wrapped publisher.
                ret = SeamPublisherWrapper.wrap(result, asyncContext);
                if (isDebug) {
                    logger.debug("Wrapped result publisher. asyncContext={}", asyncContext);
                }
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
            ret = result;
        } finally {
            trace.traceBlockEnd();
            if (ScopeUtils.isAsyncTraceEndScope(trace)) {
                deleteAsyncContext(trace, asyncContext);
            }
        }
        return ret;
    }

    private void deleteAsyncContext(Trace trace, AsyncContext asyncContext) {
        if (isDebug) {
            logger.debug("Delete async trace {}.", trace);
        }
        trace.close();
        asyncContext.close();
    }
}
