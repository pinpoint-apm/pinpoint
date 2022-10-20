/*
 * Copyright 2019 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.log4j2.interceptor;

import com.navercorp.pinpoint.bootstrap.context.RequestId;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor0;
import org.apache.logging.log4j.ThreadContext;

/**
 * @author minwoo.jung
 * @author licoco
 * @author yjqg6666
 */
public class LogEventFactoryInterceptor implements AroundInterceptor0 {

    private static final String TRANSACTION_ID = "PtxId";
    private static final String SPAN_ID = "PspanId";
    private static final String REQUEST_ID = "PreqId";
    private final TraceContext traceContext;

    public LogEventFactoryInterceptor(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target) {
        Trace trace = traceContext.currentTraceObject();

        if (trace == null) {
            ThreadContext.remove(TRANSACTION_ID);
            ThreadContext.remove(SPAN_ID);
        } else {
            ThreadContext.put(TRANSACTION_ID, trace.getTraceId().getTransactionId());
            ThreadContext.put(SPAN_ID, String.valueOf(trace.getTraceId().getSpanId()));
        }
        final Trace rawTraceObject = traceContext.currentRawTraceObject();
        if (rawTraceObject != null) {
            final RequestId requestId = rawTraceObject.getRequestId();
            if (requestId != null && requestId.isSet()) {
                ThreadContext.put(REQUEST_ID, String.valueOf(requestId.toId()));
            } else {
                ThreadContext.remove(REQUEST_ID);
            }
        } else {
            ThreadContext.remove(REQUEST_ID);
        }
    }

    @Override
    public void after(Object target, Object result, Throwable throwable) {

    }
}