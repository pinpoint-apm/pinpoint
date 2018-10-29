/*
 *  Copyright 2018 NAVER Corp.
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.navercorp.pinpoint.plugin.cxf.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.plugin.cxf.CxfPluginConstants;
import org.apache.cxf.interceptor.LoggingMessage;

/**
 * The type Cxf logging in message method interceptor.
 *
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2017/09/30
 */
public class CxfLoggingInMessageMethodInterceptor extends CxfLoggingMessageMethodInterceptor {

    /**
     * Instantiates a new Cxf logging message method interceptor.
     *
     * @param traceContext the trace context
     * @param descriptor   the descriptor
     */
    public CxfLoggingInMessageMethodInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(CxfPluginConstants.CXF_LOGGING_IN_SERVICE_TYPE);
        if (args[0] instanceof LoggingMessage) {
            recordAttributes(recorder, (LoggingMessage) args[0]);
        }
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(getMethodDescriptor());
        recorder.recordException(throwable);
    }
}