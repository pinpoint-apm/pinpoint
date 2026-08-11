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
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventResultReplaceBlockSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.reactorsupport.SeamPublisherWrapper;
import com.navercorp.pinpoint.plugin.redis.redisson.RedissonConstants;
import com.navercorp.pinpoint.plugin.redis.redisson.RedissonPluginConfig;

import java.lang.reflect.Method;

/**
 * Wrapping variant of {@link ReactiveMethodInterceptor} (config-gated by
 * {@code profiler.redis.redisson.wrap.publisher}). A reactor Mono/Flux result is replaced with
 * a wrapped one instead of relying on the accessor field the reactor plugin injects into
 * reactor.core.publisher types; any other async result keeps the original injection.
 */
public class WrappingReactiveMethodInterceptor extends SpanEventResultReplaceBlockSimpleAroundInterceptorForPlugin {
    private final boolean keyTrace;

    public WrappingReactiveMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
        final RedissonPluginConfig config = new RedissonPluginConfig(traceContext.getProfilerConfig());
        this.keyTrace = config.isKeyTrace();
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(RedissonConstants.REDISSON_REACTIVE);
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
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
    }

    @Override
    protected Object replaceResult(SpanEventRecorder recorder, Object target, Class<?> returnType, Object[] args, Object result, Throwable throwable) {
        if (throwable != null) {
            return result;
        }

        if (SeamPublisherWrapper.isWrappable(result)) {
            final AsyncContext asyncContext = recorder.recordNextAsyncContext();
            final Object wrapped = SeamPublisherWrapper.wrap(result, asyncContext);
            if (isDebug) {
                logger.debug("Wrapped result publisher. asyncContext={}", asyncContext);
            }
            return wrapped;
        }
        // non-reactor async result: keep the original injection.
        if (result instanceof AsyncContextAccessor) {
            if (AsyncContextAccessorUtils.getAsyncContext(result) == null) {
                // Avoid duplicate async context
                final AsyncContext asyncContext = recorder.recordNextAsyncContext();
                ((AsyncContextAccessor) result)._$PINPOINT$_setAsyncContext(asyncContext);
            }
        }
        return result;
    }
}
