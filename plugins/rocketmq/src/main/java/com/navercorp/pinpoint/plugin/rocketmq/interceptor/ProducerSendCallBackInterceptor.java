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

package com.navercorp.pinpoint.plugin.rocketmq.interceptor;

import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.plugin.rocketmq.RocketMQConstants;

/**
 * @author messi-gao
 */
public final class ProducerSendCallBackInterceptor {
    public static class OnSuccessInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {
        private final TraceContext traceContext;

        public OnSuccessInterceptor(
                TraceContext traceContext, MethodDescriptor methodDescriptor) {
            super(traceContext, methodDescriptor);
            this.traceContext = traceContext;
        }

        @Override
        protected void doInBeforeTrace(SpanEventRecorder spanEventRecorder, AsyncContext asyncContext, Object o,
                                       Object[] objects) {
        }

        @Override
        protected void doInAfterTrace(SpanEventRecorder spanEventRecorder, Object target, Object[] args,
                                      Object result, Throwable throwable) {
            if (logger.isDebugEnabled()) {
                logger.afterInterceptor(target, args, result, throwable);
            }

            final Trace trace = traceContext.currentTraceObject();
            if (trace == null) {
                return;
            }

            if (!trace.canSampled()) {
                return;
            }

            try {
                final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
                recorder.recordServiceType(RocketMQConstants.ROCKETMQ_CLIENT_INTERNAL);
                recorder.recordApi(methodDescriptor);
                final SendStatus sendStatus = ((SendResult) args[0]).getSendStatus();
                if (sendStatus != SendStatus.SEND_OK) {
                    recorder.recordException(new RuntimeException(sendStatus.toString()));
                }
                recorder.recordException(throwable);
            } finally {
                trace.traceBlockEnd();
            }
        }

    }

    public static class OnExceptionInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {
        private final TraceContext traceContext;

        public OnExceptionInterceptor(
                TraceContext traceContext, MethodDescriptor methodDescriptor) {
            super(traceContext, methodDescriptor);
            this.traceContext = traceContext;
        }

        @Override
        protected void doInBeforeTrace(SpanEventRecorder spanEventRecorder, AsyncContext asyncContext, Object o,
                                       Object[] objects) {

        }

        @Override
        protected void doInAfterTrace(SpanEventRecorder spanEventRecorder, Object target, Object[] args,
                                      Object result, Throwable throwable) {
            if (logger.isDebugEnabled()) {
                logger.afterInterceptor(target, args, result, throwable);
            }

            final Trace trace = traceContext.currentTraceObject();
            if (trace == null) {
                return;
            }

            if (!trace.canSampled()) {
                return;
            }

            try {
                final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
                recorder.recordServiceType(RocketMQConstants.ROCKETMQ_CLIENT_INTERNAL);
                recorder.recordApi(methodDescriptor);
                recorder.recordException(throwable);
            } finally {
                trace.traceBlockEnd();
            }
        }
    }

    private ProducerSendCallBackInterceptor() {

    }
}
