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

package com.navercorp.pinpoint.plugin.reactor.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.FluxAndMonoOperatorSubscribeInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.ReactorContextAccessor;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.ReactorContextAccessorUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;

public class ParallelFluxSubscribeInterceptor extends FluxAndMonoOperatorSubscribeInterceptor {

    public ParallelFluxSubscribeInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, ServiceType serviceType) {
        super(traceContext, methodDescriptor, serviceType);
    }

    @Override
    protected void setReactorContextToSubscriber(AsyncContext asyncContext, Object[] args) {
        if (args == null) {
            return;
        }

        for (Object arg : args) {
            if (arg instanceof Object[]) {
                final Object[] array = (Object[]) arg;
                for (Object object : array) {
                    setAsyncContext(asyncContext, object);
                }
            } else {
                setAsyncContext(asyncContext, arg);
            }
        }
    }

    private void setAsyncContext(final AsyncContext asyncContext, final Object object) {
        if (object instanceof ReactorContextAccessor) {
            final AsyncContext actualAsyncContext = ReactorContextAccessorUtils.getAsyncContext(object);
            if (actualAsyncContext == null) {
                ReactorContextAccessorUtils.setAsyncContext(asyncContext, object);
            }
        }
    }
}
