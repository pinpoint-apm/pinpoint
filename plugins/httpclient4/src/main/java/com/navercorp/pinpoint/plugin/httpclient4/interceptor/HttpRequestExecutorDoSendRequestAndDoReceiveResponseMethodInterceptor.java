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
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.Group;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.httpclient4.HttpClient4Constants;

/**
 * @author jaehong.kim
 */
@Group(value=HttpClient4Constants.HTTP_CLIENT4_SCOPE, executionPolicy=ExecutionPolicy.ALWAYS)
public class HttpRequestExecutorDoSendRequestAndDoReceiveResponseMethodInterceptor implements AroundInterceptor, HttpClient4Constants {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;
    private final InterceptorGroup interceptorGroup;


    public HttpRequestExecutorDoSendRequestAndDoReceiveResponseMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, InterceptorGroup interceptorGroup) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
        this.interceptorGroup = interceptorGroup;
    }
    
    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, methodDescriptor.getClassName(), methodDescriptor.getMethodName(), "", args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        InterceptorGroupInvocation invocation = interceptorGroup.getCurrentInvocation();
        if(invocation != null && invocation.getAttachment() != null) {
            HttpCallContext callContext = (HttpCallContext) invocation.getAttachment();
            if(methodDescriptor.getMethodName().equals("doSendRequest")) {
                callContext.setWriteBeginTime(System.currentTimeMillis());
            } else {
                callContext.setReadBeginTime(System.currentTimeMillis());
            }
            logger.debug("Set call context {}", callContext);
        }
    }

    @Override
    public void after(Object target, Object result, Throwable throwable, Object[] args) {
        if (isDebug) {
            logger.afterInterceptor(target, methodDescriptor.getClassName(), methodDescriptor.getMethodName(), "", args, result, throwable);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        InterceptorGroupInvocation invocation = interceptorGroup.getCurrentInvocation();
        if(invocation != null && invocation.getAttachment() != null) {
            HttpCallContext callContext = (HttpCallContext) invocation.getAttachment();
            if(methodDescriptor.getMethodName().equals("doSendRequest")) {
                callContext.setWriteEndTime(System.currentTimeMillis());
                callContext.setWriteFail(throwable != null);
            } else {
                callContext.setReadEndTime(System.currentTimeMillis());
                callContext.setReadFail(throwable != null);
            }
            logger.debug("Set call context {}", callContext);
        }
    }
}
