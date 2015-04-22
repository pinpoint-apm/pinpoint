/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.modifier.orm;

import com.navercorp.pinpoint.bootstrap.context.RecordableTrace;
import com.navercorp.pinpoint.bootstrap.interceptor.*;
import com.navercorp.pinpoint.common.trace.ServiceType;

/**
 * @author Hyun Jeong
 * @author netspider
 */
public abstract class SqlMapOperationInterceptor extends SpanEventSimpleAroundInterceptor {

    private final ServiceType serviceType;

    public SqlMapOperationInterceptor(ServiceType serviceType, Class<? extends SpanEventSimpleAroundInterceptor> childClazz) {
        super(childClazz);
        this.serviceType = serviceType;
    }

    @Override
    public final void doInBeforeTrace(RecordableTrace trace, final Object target, Object[] args) {
        trace.markBeforeTime();
    }

    @Override
    public final void doInAfterTrace(RecordableTrace trace, Object target, Object[] args, Object result, Throwable throwable) {
        trace.recordServiceType(this.serviceType);
        trace.recordException(throwable);
        if (args != null && args.length > 0) {
            trace.recordApiCachedString(getMethodDescriptor(), (String)args[0], 0);
        } else {
            trace.recordApi(getMethodDescriptor());
        }
        trace.markAfterTime();
    }

}
