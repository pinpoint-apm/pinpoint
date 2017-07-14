/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.plugin.hystrix.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.hystrix.HystrixPluginConstants;

/**
 * @author HyunGil Jeong
 */
public class HystrixCommandGetFallbackOrThrowExceptionInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public HystrixCommandGetFallbackOrThrowExceptionInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(HystrixPluginConstants.HYSTRIX_INTERNAL_SERVICE_TYPE);
        recorder.recordException(throwable);
        // R getFallbackOrThrowException(HystrixEventType, FailureType, String, Exception)
        if (args != null && args.length == 4) {
            Object exception = args[3];
            if (exception != null) {
                recorder.recordAttribute(HystrixPluginConstants.HYSTRIX_FALLBACK_CAUSE_ANNOTATION_KEY, exception.toString());
            } else {
                Object message = args[2];
                if (message != null) {
                    recorder.recordAttribute(HystrixPluginConstants.HYSTRIX_FALLBACK_CAUSE_ANNOTATION_KEY, message.toString());
                }
            }
        }
    }
}
