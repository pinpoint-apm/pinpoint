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

package com.navercorp.pinpoint.plugin.activemq.client.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.activemq.client.ActiveMQClientConstants;

/**
 * @author HyunGil Jeong
 */
public class MessageDispatchChannelDequeueInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public MessageDispatchChannelDequeueInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    // These methods may be polled, producing a lot of garbage log.
    // Instead, only log when the method is actually traced.
    @Override
    protected void logBeforeInterceptor(Object target, Object[] args) {
        return;
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        if (isDebug) {
            super.logBeforeInterceptor(target, args);
        }
    }

    // These methods may be polled, producing a lot of garbage log.
    // Instead, only log when the method is actually traced.
    @Override
    protected void logAfterInterceptor(Object target, Object[] args, Object result, Throwable throwable) {
        return;
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            super.logAfterInterceptor(target, args, result, throwable);
        }
        recorder.recordServiceType(ActiveMQClientConstants.ACTIVEMQ_CLIENT_INTERNAL);
        recorder.recordApi(getMethodDescriptor());
        if (throwable != null) {
            recorder.recordException(throwable);
        }
    }
}
