/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.redis.lettuce.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.redis.lettuce.EndPointAccessor;
import com.navercorp.pinpoint.plugin.redis.lettuce.LettuceConstants;
import com.navercorp.pinpoint.plugin.redis.lettuce.StatefulConnectionGetter;

/**
 * @author jaehong.kim
 */
public class LettuceMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public LettuceMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        final String endPoint = toEndPoint(target);
        recorder.recordApi(getMethodDescriptor());
        recorder.recordEndPoint(endPoint != null ? endPoint : "Unknown");
        recorder.recordDestinationId(LettuceConstants.REDIS_LETTUCE.getName());
        recorder.recordServiceType(LettuceConstants.REDIS_LETTUCE);
        recorder.recordException(throwable);

        if(result instanceof AsyncContextAccessor) {
            if(AsyncContextAccessorUtils.getAsyncContext(result) == null) {
                // Avoid duplicate async context
                final AsyncContext asyncContext = recorder.recordNextAsyncContext();
                ((AsyncContextAccessor)result)._$PINPOINT$_setAsyncContext(asyncContext);
            }
        }
    }

    private String toEndPoint(final Object target) {
        if (!(target instanceof StatefulConnectionGetter)) {
            return null;
        }

        final Object connection = ((StatefulConnectionGetter) target)._$PINPOINT$_getConnection();
        if (!(connection instanceof EndPointAccessor)) {
            return null;
        }

        return ((EndPointAccessor) connection)._$PINPOINT$_getEndPoint();
    }
}