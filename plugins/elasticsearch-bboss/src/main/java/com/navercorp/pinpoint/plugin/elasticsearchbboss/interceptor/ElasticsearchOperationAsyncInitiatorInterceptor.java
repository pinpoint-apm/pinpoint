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
package com.navercorp.pinpoint.plugin.elasticsearchbboss.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.plugin.elasticsearchbboss.ElasticsearchConstants;

/**
 * @author yinbp[yin-bp@163.com]
 */
public class ElasticsearchOperationAsyncInitiatorInterceptor  implements AroundInterceptor {

    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;
    private final InterceptorScope scope;

    public ElasticsearchOperationAsyncInitiatorInterceptor(TraceContext traceContext, MethodDescriptor descriptor, InterceptorScope scope) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.scope = scope;
    }

    @Override
    public void before(Object target, Object arg[]) {
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(ElasticsearchConstants.ELASTICSEARCH);
        recorder.recordApi(descriptor);

        // To trace async invocations, you have to create AsyncContext like below, automatically attaching it to the current span event.
        AsyncContext asyncContext = recorder.recordNextAsyncContext();

        // Finally, you have to pass the AsyncContext to the thread which handles the async task.
        // How to do this depends on the target library implementation.
        //
        // In this sample, we set the id as scope invocation attachment like below to pass it to the constructor interceptor of TargetClass12_Worker which has run() method that handles the async task.
        // Then the constructor interceptor will get the attached AsyncContext and set to the initializing TargetClass12_Worker object.
        scope.getCurrentInvocation().setAttachment(asyncContext);
    }

    @Override
    public void after(Object target, Object arg[], Object result, Throwable throwable) {
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
