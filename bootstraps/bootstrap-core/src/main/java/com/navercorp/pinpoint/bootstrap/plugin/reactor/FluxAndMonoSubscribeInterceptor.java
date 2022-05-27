/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.reactor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.common.trace.ServiceType;

public class FluxAndMonoSubscribeInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {
    private final ServiceType serviceType;

    public FluxAndMonoSubscribeInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, ServiceType serviceType) {
        super(traceContext, methodDescriptor);
        this.serviceType = serviceType;
    }

    // AsyncContext must exist in Target for tracking.
    public AsyncContext getAsyncContext(Object target, Object[] args) {
        if (checkTargetReactorContextAccessor(target, args)) {
            return AsyncContextAccessorUtils.getAsyncContext(target);
        }
        if (checkTargetAsyncContextAccessor(target, args)) {
            return AsyncContextAccessorUtils.getAsyncContext(target);
        }
        if (checkSubscriberReactorContextAccessor(target, args)) {
            return AsyncContextAccessorUtils.getAsyncContext(target);
        }
        return AsyncContextAccessorUtils.getAsyncContext(target);
    }

    private boolean checkTargetReactorContextAccessor(final Object target, final Object[] args) {
        final AsyncContext asyncContext = ReactorContextAccessorUtils.getAsyncContext(target);
        if (asyncContext != null) {
            setReactorContextToSubscriber(asyncContext, args);
            return true;
        }
        return false;
    }

    private boolean checkTargetAsyncContextAccessor(final Object target, final Object[] args) {
        final AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(target);
        if (asyncContext != null) {
            setReactorContextToTarget(asyncContext, target);
            setReactorContextToSubscriber(asyncContext, args);
            return true;
        }
        return false;
    }

    private boolean checkSubscriberReactorContextAccessor(final Object target, final Object[] args) {
        final AsyncContext asyncContext = ReactorContextAccessorUtils.getAsyncContext(args, 0);
        if (asyncContext != null) {
            setReactorContextToTarget(asyncContext, target);
            return true;
        }
        return false;
    }

    protected void setReactorContextToTarget(AsyncContext asyncContext, Object target) {
        final AsyncContext targetAsyncContext = ReactorContextAccessorUtils.getAsyncContext(target);
        if (targetAsyncContext == null) {
            ReactorContextAccessorUtils.setAsyncContext(asyncContext, target);
        }
    }

    protected void setReactorContextToSubscriber(AsyncContext asyncContext, Object[] args) {
        final AsyncContext subscriberAsyncContext = ReactorContextAccessorUtils.getAsyncContext(args, 0);
        if (subscriberAsyncContext == null) {
            ReactorContextAccessorUtils.setAsyncContext(asyncContext, args, 0);
        }
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
    }

    public AsyncContext getAsyncContext(Object target, Object[] args, Object result, Throwable throwable) {
        return AsyncContextAccessorUtils.getAsyncContext(target);
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(serviceType);
        recorder.recordException(throwable);
    }
}
