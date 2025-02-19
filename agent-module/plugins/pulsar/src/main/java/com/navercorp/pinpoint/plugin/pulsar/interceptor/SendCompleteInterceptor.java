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
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.plugin.pulsar.PulsarConstants;
import org.apache.pulsar.client.impl.OpSendMsgStats;

/**
 * @author zhouzixin@apache.org
 */
public class SendCompleteInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {

    public SendCompleteInterceptor(final TraceContext traceContext, final MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordServiceType(PulsarConstants.PULSAR_CLIENT_INTERNAL);
        if (args[1] instanceof OpSendMsgStats) {
            OpSendMsgStats sendMsgStats = (OpSendMsgStats) args[1];
            recorder.recordAttribute(PulsarConstants.PULSAR_SEQUENCE_ID_ANNOTATION_KEY, sendMsgStats.getSequenceId());
            recorder.recordAttribute(PulsarConstants.PULSAR_RETRY_COUNT_ANNOTATION_KEY, sendMsgStats.getRetryCount());
        }
        recorder.recordApi(methodDescriptor);
        recorder.recordException((Throwable) args[0]);
    }
}
