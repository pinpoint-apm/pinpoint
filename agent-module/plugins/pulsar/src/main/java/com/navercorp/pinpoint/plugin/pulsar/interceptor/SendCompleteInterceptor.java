/*
 * Copyright 2025 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.pulsar.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextTraceBlockSimpleAroundInterceptor;
import com.navercorp.pinpoint.common.util.ArrayArgumentUtils;
import com.navercorp.pinpoint.plugin.pulsar.PulsarConstants;
import com.navercorp.pinpoint.plugin.pulsar.field.getter.RetryCountGetter;
import com.navercorp.pinpoint.plugin.pulsar.field.getter.SequenceIdGetter;

/**
 * @author zhouzixin@apache.org
 */
public class SendCompleteInterceptor extends AsyncContextTraceBlockSimpleAroundInterceptor {

    public SendCompleteInterceptor(final TraceContext traceContext, final MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
        recorder.recordServiceType(PulsarConstants.PULSAR_CLIENT_INTERNAL);
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        // OpSendMsgStatsImpl (relocated in the shaded pulsar-client 5.x), absent on sendComplete(Throwable)
        final Object sendMsgStats = ArrayArgumentUtils.getArgument(args, 1, Object.class);
        if (sendMsgStats instanceof SequenceIdGetter) {
            final long sequenceId = ((SequenceIdGetter) sendMsgStats)._$PINPOINT$_getSequenceId();
            recorder.recordAttribute(PulsarConstants.PULSAR_SEQUENCE_ID_ANNOTATION_KEY, sequenceId);
        }
        if (sendMsgStats instanceof RetryCountGetter) {
            final int retryCount = ((RetryCountGetter) sendMsgStats)._$PINPOINT$_getRetryCount();
            recorder.recordAttribute(PulsarConstants.PULSAR_RETRY_COUNT_ANNOTATION_KEY, retryCount);
        }
        recorder.recordApi(methodDescriptor);
        recorder.recordException(ArrayArgumentUtils.getArgument(args, 0, Throwable.class));
    }
}
