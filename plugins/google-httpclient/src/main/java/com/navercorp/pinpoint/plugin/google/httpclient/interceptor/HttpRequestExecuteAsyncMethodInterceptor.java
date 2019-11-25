/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.google.httpclient.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.plugin.google.httpclient.HttpClientConstants;

/**
 * 
 * @author jaehong.kim
 *
 */
public class HttpRequestExecuteAsyncMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    private final InterceptorScope interceptorScope;

    public HttpRequestExecuteAsyncMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, InterceptorScope interceptorScope) {
        super(traceContext, methodDescriptor);
        this.interceptorScope = interceptorScope;
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        // set asynchronous trace
        final AsyncContext asyncContext = recorder.recordNextAsyncContext();

        final InterceptorScopeInvocation transaction = interceptorScope.getCurrentInvocation();
        if (transaction != null) {
            transaction.setAttachment(asyncContext);
            if (isDebug) {
                logger.debug("Set AsyncContext {}", asyncContext);
            }
        }
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(HttpClientConstants.HTTP_CLIENT_INTERNAL);
        recorder.recordException(throwable);

        // remove async id.
        InterceptorScopeInvocation transaction = interceptorScope.getCurrentInvocation();
        if (transaction != null) {
            // clear
            transaction.removeAttachment();
        }
    }

}