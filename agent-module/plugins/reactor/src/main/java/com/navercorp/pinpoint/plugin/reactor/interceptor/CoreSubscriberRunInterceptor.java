/*
 * Copyright 2025 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.reactor.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.common.trace.ServiceType;

public class CoreSubscriberRunInterceptor extends AsyncContextSpanEventApiIdAwareAroundInterceptor {
    private final ServiceType serviceType;

    public CoreSubscriberRunInterceptor(TraceContext traceContext, ServiceType serviceType) {
        super(traceContext);
        this.serviceType = serviceType;
    }

    @Override
    public AsyncContext getAsyncContext(Object target, Object[] args) {
        return AsyncContextAccessorUtils.getAsyncContext(target);
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, int apidId, Object[] args) {
    }

    @Override
    public AsyncContext getAsyncContext(Object target, Object[] args, Object result, Throwable throwable) {
        return AsyncContextAccessorUtils.getAsyncContext(target);
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        recorder.recordApiId(apiId);
        recorder.recordServiceType(serviceType);
        recorder.recordException(throwable);
    }
}