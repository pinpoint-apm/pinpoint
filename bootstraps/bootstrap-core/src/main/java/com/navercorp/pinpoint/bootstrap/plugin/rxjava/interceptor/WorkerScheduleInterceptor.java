/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.plugin.rxjava.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author HyunGil Jeong
 */
public class WorkerScheduleInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    private final ServiceType serviceType;

    public WorkerScheduleInterceptor(TraceContext traceContext, MethodDescriptor descriptor, ServiceType serviceType) {
        super(traceContext, descriptor);
        this.serviceType = serviceType;
    }

    @Override
    protected void logBeforeInterceptor(Object target, Object[] args) {
        // may be called from an event loop, log only when tracing
    }

    private void logBeforeInterceptor0(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        logBeforeInterceptor0(target, args);
        if (args != null && args.length > 0) {
            if (args[0] instanceof AsyncContextAccessor) {
                AsyncContext asyncContext = recorder.recordNextAsyncContext();
                ((AsyncContextAccessor) args[0])._$PINPOINT$_setAsyncContext(asyncContext);
            }
        }
    }

    @Override
    protected void logAfterInterceptor(Object target, Object[] args, Object result, Throwable throwable) {
        // may be called from an event loop, log only when tracing
    }

    private void logAfterInterceptor0(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        logAfterInterceptor0(target, args, result, throwable);
        recorder.recordServiceType(serviceType);
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
    }
}
