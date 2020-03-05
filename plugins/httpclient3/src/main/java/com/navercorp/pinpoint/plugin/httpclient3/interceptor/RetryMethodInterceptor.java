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

package com.navercorp.pinpoint.plugin.httpclient3.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.httpclient3.HttpClient3Constants;

/**
 * @author Minwoo Jung
 * @author jaehong.kim
 */
public class RetryMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public RetryMethodInterceptor(TraceContext context, MethodDescriptor methodDescriptor) {
        super(context, methodDescriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(HttpClient3Constants.HTTP_CLIENT_3_INTERNAL);
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);

        final String retryMessage = getRetryMessage(args);
        recorder.recordAttribute(AnnotationKey.HTTP_INTERNAL_DISPLAY, retryMessage);

        if (result != null) {
            recorder.recordAttribute(AnnotationKey.RETURN_DATA, result);
        }
    }

    private String getRetryMessage(Object[] args) {
        if (args == null) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        if (args.length >= 2 && args[1] instanceof Exception) {
            sb.append(args[1].getClass().getName()).append(", ");
        }
        if (args.length >= 3 && args[2] instanceof Integer) {
            sb.append(args[2]);
        }
        return sb.toString();
    }

}