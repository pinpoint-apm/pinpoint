/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.ibatis.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.ArrayUtils;

/**
 * @author HyunGil Jeong
 */
public class SqlMapOperationInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    private final ServiceType serviceType;

    public SqlMapOperationInterceptor(TraceContext context, MethodDescriptor descriptor, ServiceType serviceType) {
        super(context, descriptor);
        this.serviceType = serviceType;
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        // do nothing
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result,
            Throwable throwable) {
        recorder.recordServiceType(this.serviceType);
        recorder.recordException(throwable);
        if (ArrayUtils.hasLength(args)) {
            recorder.recordApiCachedString(getMethodDescriptor(), (String)args[0], 0);
        } else {
            recorder.recordApi(getMethodDescriptor());
        }
    }

}
