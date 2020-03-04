/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.resttemplate.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.resttemplate.ExecAsyncResultMethodDescriptor;
import com.navercorp.pinpoint.plugin.resttemplate.RestTemplateConstants;
import com.navercorp.pinpoint.plugin.resttemplate.field.accessor.TraceFutureFlagAccessor;

import org.springframework.util.concurrent.ListenableFuture;

/**
 * @author Taejin Koo
 */
public class AsyncHttpRequestInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    private static final ExecAsyncResultMethodDescriptor execAsyncResultMethodDescriptor = new ExecAsyncResultMethodDescriptor();


    public AsyncHttpRequestInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);

        traceContext.cacheApi(execAsyncResultMethodDescriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(RestTemplateConstants.SERVICE_TYPE);
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);

        // create virtual-method
        traceAndRecordFuture(result, throwable);
    }

    // reason for virtual Method
    ////////////////////////////////////////////////////
    // 1. if virtualMethod not crated
    // executeAsync
    //   - Asynchronous Invocation (for future set)
    //   - some trace (like connect, write, etc ...)
    ////////////////////////////
    // 2. if virtualMethod crated
    // executeAsync
    //
    //   - some trace (like connect, write, etc ...)
    //   - Asynchronous Invocation (for future set)
    ////////////////////////////////////////////////////
    private void traceAndRecordFuture(Object result, Throwable throwable) {
        if (throwable != null) {
            return;
        }
        if (!(result instanceof ListenableFuture)) {
            return;
        }

        final Trace virtualMethodTrace = traceContext.currentTraceObject();
        try {
            SpanEventRecorder recorder = virtualMethodTrace.traceBlockBegin();
            recorder.recordServiceType(RestTemplateConstants.SERVICE_TYPE);
            recorder.recordApi(execAsyncResultMethodDescriptor);
            if (isAsynchronousInvocation(result)) {
                // set asynchronous trace
                final AsyncContext asyncContext = recorder.recordNextAsyncContext();
                ((AsyncContextAccessor) result)._$PINPOINT$_setAsyncContext(asyncContext);
                if (isDebug) {
                    logger.debug("Set AsyncContext {}", asyncContext);
                }

                if (result instanceof TraceFutureFlagAccessor) {
                    ((TraceFutureFlagAccessor) result)._$PINPOINT$_setTraceFlag(true);
                }
            }
        } finally {
            virtualMethodTrace.traceBlockEnd();
        }
    }

    private boolean isAsynchronousInvocation(Object result) {
        if (!(result instanceof AsyncContextAccessor)) {
            logger.debug("Invalid result object. Need accessor({}).", AsyncContextAccessor.class.getName());
            return false;
        }

        return true;
    }

}