/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.redis.redisson.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.redis.redisson.RedissonConstants;
import com.navercorp.pinpoint.plugin.redis.redisson.RedissonPluginConfig;
import org.redisson.client.protocol.RedisCommand;

import java.lang.reflect.Method;

/**
 * @author jaehong.kim
 */
public class ReactiveMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {
    private final boolean keyTrace;

    public ReactiveMethodInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
        final RedissonPluginConfig config = new RedissonPluginConfig(traceContext.getProfilerConfig());
        this.keyTrace = config.isKeyTrace();
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        if (result instanceof AsyncContextAccessor) {
            if (AsyncContextAccessorUtils.getAsyncContext(result) == null) {
                // Avoid duplicate async context
                final AsyncContext asyncContext = recorder.recordNextAsyncContext();
                ((AsyncContextAccessor) result)._$PINPOINT$_setAsyncContext(asyncContext);
            }
        }

        if (this.keyTrace) {
            Method method = (Method) args[0];
            if (method != null && StringUtils.hasLength(method.getName())) {
                recorder.recordAttribute(AnnotationKey.ARGS0, method.getName());
            }
        }

        recorder.recordApi(getMethodDescriptor());
        recorder.recordServiceType(RedissonConstants.REDISSON_REACTIVE);
        recorder.recordException(throwable);
    }
}
