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

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.plugin.rocketmq.RocketMQConstants;

/**
 * @author messi-gao
 */
public final class ConsumerMessageListenerConcurrentlyInterceptor extends ConsumerMessageEntryPointInterceptor {

    public ConsumerMessageListenerConcurrentlyInterceptor(
            TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result,
                                  Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        if (result instanceof ConsumeConcurrentlyStatus) {
            final ConsumeConcurrentlyStatus status = (ConsumeConcurrentlyStatus) result;
            recorder.recordAttribute(RocketMQConstants.ROCKETMQ_SEND_STATUS_ANNOTATION_KEY, status.name());
        }
        if (throwable != null) {
            recorder.recordException(throwable);
        }
    }
}
