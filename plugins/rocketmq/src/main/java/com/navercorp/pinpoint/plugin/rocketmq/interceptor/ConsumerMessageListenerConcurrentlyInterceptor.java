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

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.rocketmq.RocketMQConstants;

/**
 * @author messi-gao
 */
public final class ConsumerMessageListenerConcurrentlyInterceptor extends ConsumerMessageEntryPointInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;

    public ConsumerMessageListenerConcurrentlyInterceptor(
            TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
        this.traceContext = traceContext;
        this.descriptor = descriptor;
    }


    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
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
            recorder.recordApi(descriptor);
            ConsumeConcurrentlyStatus status = (ConsumeConcurrentlyStatus) result;
            if (status == ConsumeConcurrentlyStatus.RECONSUME_LATER) {
                recorder.recordException(new RuntimeException(status.toString()));
            }
            recorder.recordException(throwable);
        } finally {
            trace.traceBlockEnd();
        }
    }
}
