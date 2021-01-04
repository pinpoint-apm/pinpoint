/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.rocketmq.interceptor;

import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.rocketmq.RocketMQConstants;

/**
 * @author messi-gao
 */
public final class ProducerSendCallBackInterceptor {
    public static class ConstructInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {
        public ConstructInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
            super(traceContext, descriptor);
        }

        @Override
        protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
            final AsyncContext asyncContext = recorder.recordNextAsyncContext();
            ((AsyncContextAccessor) target)._$PINPOINT$_setAsyncContext(asyncContext);
        }

        @Override
        protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result,
                                      Throwable throwable) {
            recorder.recordServiceType(RocketMQConstants.ROCKETMQ_CLIENT_INTERNAL);
            recorder.recordApi(methodDescriptor);
            recorder.recordException(throwable);
        }
    }

    public static class OnSuccessInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {
        public OnSuccessInterceptor(
                TraceContext traceContext, MethodDescriptor methodDescriptor) {
            super(traceContext, methodDescriptor);
        }

        @Override
        protected void doInBeforeTrace(SpanEventRecorder spanEventRecorder, AsyncContext asyncContext, Object o,
                                       Object[] objects) {
        }

        @Override
        protected void doInAfterTrace(SpanEventRecorder spanEventRecorder, Object target, Object[] args,
                                      Object result, Throwable throwable) {
            spanEventRecorder.recordServiceType(RocketMQConstants.ROCKETMQ_CLIENT_INTERNAL);
            spanEventRecorder.recordApi(methodDescriptor);
            final Object arg = args[0];
            if (arg instanceof SendResult) {
                final SendStatus sendStatus = ((SendResult) arg).getSendStatus();
                spanEventRecorder.recordAttribute(RocketMQConstants.ROCKETMQ_SEND_STATUS_ANNOTATION_KEY,
                                                  sendStatus.name());
            }
            if (throwable != null) {
                spanEventRecorder.recordException(throwable);
            }
        }

    }

    public static class OnExceptionInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {

        public OnExceptionInterceptor(
                TraceContext traceContext, MethodDescriptor methodDescriptor) {
            super(traceContext, methodDescriptor);
        }

        @Override
        protected void doInBeforeTrace(SpanEventRecorder spanEventRecorder, AsyncContext asyncContext, Object o,
                                       Object[] objects) {

        }

        @Override
        protected void doInAfterTrace(SpanEventRecorder spanEventRecorder, Object target, Object[] args,
                                      Object result, Throwable throwable) {
            spanEventRecorder.recordServiceType(RocketMQConstants.ROCKETMQ_CLIENT_INTERNAL);
            spanEventRecorder.recordApi(methodDescriptor);
            spanEventRecorder.recordException((Throwable) args[0]);
        }
    }

    private ProducerSendCallBackInterceptor() {

    }
}
