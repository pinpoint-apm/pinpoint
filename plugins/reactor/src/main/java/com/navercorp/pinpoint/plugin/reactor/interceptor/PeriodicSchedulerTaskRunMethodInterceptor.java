/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.reactor.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanRecursiveAroundInterceptor;
import com.navercorp.pinpoint.plugin.reactor.ReactorConstants;
import com.navercorp.pinpoint.plugin.reactor.ReactorPeriodicSchedulerMethodDescriptor;

/**
 * @author jaehong.kim
 */
public class PeriodicSchedulerTaskRunMethodInterceptor extends SpanRecursiveAroundInterceptor {
    private static final String SCOPE_NAME = "##REACTOR_SCHEDULER_PERIODIC_TRACE";
    private static final MethodDescriptor REACTOR_PERIODIC_SCHEDULER_METHOD_DESCRIPTOR = new ReactorPeriodicSchedulerMethodDescriptor();

    public PeriodicSchedulerTaskRunMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor, SCOPE_NAME);
        traceContext.cacheApi(REACTOR_PERIODIC_SCHEDULER_METHOD_DESCRIPTOR);
    }

    @Override
    public Trace createTrace(Object target, Object[] args) {
        if (AsyncContextAccessorUtils.getAsyncContext(target) == null) {
            return null;
        }

        final Trace trace = traceContext.newTraceObject();
        if (trace.canSampled()) {
            final SpanRecorder recorder = trace.getSpanRecorder();
            // You have to record a service type within Server range.
            recorder.recordServiceType(ReactorConstants.REACTOR_SCHEDULER);
            recorder.recordApi(REACTOR_PERIODIC_SCHEDULER_METHOD_DESCRIPTOR);
            recorder.recordRemoteAddress("LOCAL");
            recorder.recordRpcName("/schedulePeriodically");
            recorder.recordEndPoint("/");
        }
        return trace;
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(ReactorConstants.REACTOR_NETTY);
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
    }
}
