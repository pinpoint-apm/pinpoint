/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.rabbitmq.client.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.rabbitmq.client.RabbitMQClientConstants;

/**
 * @author HyunGil Jeong
 */
public class QueueingConsumerOnNextInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public QueueingConsumerOnNextInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    // These methods may be polled, producing a lot of garbage log.
    // Instead, only log when the method is actually traced.
    @Override
    protected void logBeforeInterceptor(Object target, Object[] args) {
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        if (isDebug) {
            super.logBeforeInterceptor(target, args);
        }
    }

    // These methods may be polled, producing a lot of garbage log.
    // Instead, only log when the method is actually traced.
    @Override
    protected void logAfterInterceptor(Object target, Object[] args, Object result, Throwable throwable) {
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            super.logAfterInterceptor(target, args, result, throwable);
        }
        recorder.recordServiceType(RabbitMQClientConstants.RABBITMQ_CLIENT_INTERNAL);
        recorder.recordApi(getMethodDescriptor());
        if (throwable != null) {
            recorder.recordException(throwable);
        }
    }
}
