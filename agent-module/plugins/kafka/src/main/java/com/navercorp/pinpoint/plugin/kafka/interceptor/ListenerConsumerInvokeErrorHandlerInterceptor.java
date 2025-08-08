/*
 * Copyright 2025 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.kafka.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.kafka.KafkaConfig;
import com.navercorp.pinpoint.plugin.kafka.KafkaConstants;

public class ListenerConsumerInvokeErrorHandlerInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {
    final boolean markError;

    public ListenerConsumerInvokeErrorHandlerInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
        final KafkaConfig config = new KafkaConfig(traceContext.getProfilerConfig());
        this.markError = config.isKafkaMessageListenerContainerMarkError();
    }

    @Override
    public Trace currentTrace() {
        if (markError) {
            return traceContext.currentTraceObject();
        }
        return null;
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) throws Exception {
        recorder.recordServiceType(KafkaConstants.KAFKA_CLIENT_INTERNAL);
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) throws Exception {
        recorder.recordApi(methodDescriptor);
        final RuntimeException runtimeException = ArrayArgumentUtils.getArgument(args, 2, RuntimeException.class);
        if (runtimeException != null) {
            recorder.recordException(runtimeException);
        }
    }
}
