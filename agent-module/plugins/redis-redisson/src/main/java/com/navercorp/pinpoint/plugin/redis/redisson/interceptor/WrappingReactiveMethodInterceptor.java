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

package com.navercorp.pinpoint.plugin.redis.redisson.interceptor;

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
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.reactorsupport.SeamPublisherWrapper;
import com.navercorp.pinpoint.plugin.redis.redisson.RedissonConstants;
import com.navercorp.pinpoint.plugin.redis.redisson.RedissonPluginConfig;

import java.lang.reflect.Method;
import java.util.Objects;

/**
 * Wrapping variant of {@link ReactiveMethodInterceptor} (config-gated by
 * {@code profiler.redis.redisson.wrap.publisher}). A reactor Mono/Flux result is replaced with
 * a wrapped one instead of relying on the accessor field the reactor plugin injects into
 * reactor.core.publisher types; any other async result keeps the original injection.
 */
public class WrappingReactiveMethodInterceptor implements ResultReplaceAroundInterceptor {
    private final PluginLogger logger = PluginLogManager.getLogger(getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;
    private final boolean keyTrace;

    public WrappingReactiveMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        this.traceContext = Objects.requireNonNull(traceContext, "traceContext");
        this.methodDescriptor = Objects.requireNonNull(methodDescriptor, "methodDescriptor");
        final RedissonPluginConfig config = new RedissonPluginConfig(traceContext.getProfilerConfig());
        this.keyTrace = config.isKeyTrace();
    }

    @Override
    public void before(Object target, Class<?> returnType, Object[] args) {
        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(RedissonConstants.REDISSON_REACTIVE);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    @Override
    public Object after(Object target, Class<?> returnType, Object[] args, Object result, Throwable throwable) {
        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return result;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();

            Object ret = result;
            if (throwable == null) {
                if (SeamPublisherWrapper.isWrappable(result)) {
                    final AsyncContext asyncContext = recorder.recordNextAsyncContext();
                    ret = SeamPublisherWrapper.wrap(result, asyncContext);
                    if (isDebug) {
                        logger.debug("Wrapped result publisher. asyncContext={}", asyncContext);
                    }
                } else if (result instanceof AsyncContextAccessor) {
                    // non-reactor async result: keep the original injection.
                    if (AsyncContextAccessorUtils.getAsyncContext(result) == null) {
                        // Avoid duplicate async context
                        final AsyncContext asyncContext = recorder.recordNextAsyncContext();
                        ((AsyncContextAccessor) result)._$PINPOINT$_setAsyncContext(asyncContext);
                    }
                }
            }

            if (this.keyTrace) {
                Method method = ArrayArgumentUtils.getArgument(args, 0, Method.class);
                if (method == null) {
                    // redisson 3.17+: execute(Callable, Method)
                    method = ArrayArgumentUtils.getArgument(args, 1, Method.class);
                }
                if (method != null && StringUtils.hasLength(method.getName())) {
                    recorder.recordAttribute(AnnotationKey.ARGS0, method.getName());
                }
            }

            recorder.recordApi(methodDescriptor);
            recorder.recordException(throwable);
            return ret;
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
