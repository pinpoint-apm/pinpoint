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
package com.navercorp.pinpoint.plugin.hbase.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.plugin.hbase.HbasePluginConstants;

/**
 * The type Hbase client main interceptor.
 *
 * @author Victor.Zxy
 * @version 1.8.1
 * @since 2018/10/18
 */
public class HbaseClientMainInterceptor implements AroundInterceptor {
    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;
    private final InterceptorScope scope;

    /**
     * Instantiates a new Hbase client main interceptor.
     *
     * @param traceContext the trace context
     * @param descriptor   the descriptor
     * @param scope        the scope
     */
    public HbaseClientMainInterceptor(TraceContext traceContext, MethodDescriptor descriptor, InterceptorScope scope) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.scope = scope;
    }

    @Override
    public void before(Object target, Object[] args) {
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(HbasePluginConstants.HBASE_ASYNC_CLIENT);
        recorder.recordApi(descriptor, args);

        // To trace async invocations, you have to create AsyncContext like below, automatically attaching it to the current span event.
        AsyncContext asyncContext = recorder.recordNextAsyncContext();

        // Finally, you have to pass the AsyncContext to the thread which handles the async task.
        // How to do this depends on the target library implementation.
        scope.getCurrentInvocation().setAttachment(asyncContext);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            if (throwable != null) {
                SpanEventRecorder recorder = trace.currentSpanEventRecorder();
                recorder.recordException(throwable);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }
}
