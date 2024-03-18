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

package com.navercorp.pinpoint.plugin.httpclient4.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.httpclient4.HttpCallContext;

/**
 * @author jaehong.kim
 */
public class HttpRequestExecutorDoSendRequestAndDoReceiveResponseMethodInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;
    private final InterceptorScope interceptorScope;


    public HttpRequestExecutorDoSendRequestAndDoReceiveResponseMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, InterceptorScope interceptorScope) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
        this.interceptorScope = interceptorScope;
    }
    
    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        final InterceptorScopeInvocation invocation = interceptorScope.getCurrentInvocation();
        final Object attachment = getAttachment(invocation);
        if (attachment instanceof HttpCallContext) {
            HttpCallContext callContext = (HttpCallContext) attachment;
            if (methodDescriptor.getMethodName().equals("doSendRequest")) {
                callContext.setWriteBeginTime(System.currentTimeMillis());
            } else {
                callContext.setReadBeginTime(System.currentTimeMillis());
            }
            if(isDebug) {
                logger.debug("Set call context {}", callContext);
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        final InterceptorScopeInvocation invocation = interceptorScope.getCurrentInvocation();
        final Object attachment = getAttachment(invocation);
        if (attachment instanceof HttpCallContext) {
            HttpCallContext callContext = (HttpCallContext) attachment;
            if (methodDescriptor.getMethodName().equals("doSendRequest")) {
                callContext.setWriteEndTime(System.currentTimeMillis());
                callContext.setWriteFail(throwable != null);
            } else {
                callContext.setReadEndTime(System.currentTimeMillis());
                callContext.setReadFail(throwable != null);
            }
            if (isDebug) {
                logger.debug("Set call context {}", callContext);
            }
        }
    }

    private Object getAttachment(InterceptorScopeInvocation invocation) {
        if (invocation == null) {
            return null;
        }
        return invocation.getAttachment();
    }


}
