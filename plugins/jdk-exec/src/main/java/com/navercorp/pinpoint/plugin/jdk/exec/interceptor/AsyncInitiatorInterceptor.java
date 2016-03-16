package com.navercorp.pinpoint.plugin.jdk.exec.interceptor;

/**
 * Copyright 2014 NAVER Corp.
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
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor1;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.plugin.jdk.exec.CacheMap;
import com.navercorp.pinpoint.plugin.jdk.exec.JdkExecConstants;

/**
 * To trace async invocations, you have to begin with the method initiating an async task.
 */
public class AsyncInitiatorInterceptor implements AroundInterceptor1 {
    private final MethodDescriptor descriptor;
    private final TraceContext traceContext;
    private final InterceptorScope scope;

    public AsyncInitiatorInterceptor(TraceContext traceContext, MethodDescriptor descriptor, InterceptorScope scope) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        this.scope = scope;
    }

    @Override
    public void before(Object target, Object arg0) {
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(JdkExecConstants.SERVICE_TYPE);
        recorder.recordApi(descriptor, new Object[] { arg0 });

        // To trace async invocations, you have to get async trace id like below.
        AsyncTraceId asyncTraceId = trace.getAsyncTraceId();

        // Then record the AsyncTraceId as next async id.
        recorder.recordNextAsyncId(asyncTraceId.getAsyncId());

        // Finally, you have to pass the AsyncTraceId to the thread which handles the async task.
        // How to do that depends on the target library implementation.
        //
        // In this sample, we set the id as scope invocation attachment like below to pass it to the constructor interceptor of TargetClass12_Worker which has run() method that handles the async task.
        // Then the constructor interceptor will get the attached id and set to the initializing TargetClass12_Worker object.
        scope.getCurrentInvocation().setAttachment(asyncTraceId);

    }

    @Override
    public void after(Object target, Object arg0, Object result, Throwable throwable) {
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        AsyncTraceId asyncTraceId = (AsyncTraceId)scope.getCurrentInvocation().getAttachment();

        //save by hashCode to avoid
        CacheMap.getInstance(JdkExecConstants.ASYNC_ID_MAP).put(result.hashCode(), asyncTraceId);

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
