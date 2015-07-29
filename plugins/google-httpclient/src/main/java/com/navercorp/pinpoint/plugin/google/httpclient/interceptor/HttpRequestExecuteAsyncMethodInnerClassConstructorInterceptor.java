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

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Group;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.plugin.google.httpclient.HttpClientConstants;

/**
 * 
 * @author jaehong.kim
 *
 */
@Group(value = HttpClientConstants.EXECUTE_ASYNC_SCOPE, executionPoint = ExecutionPolicy.ALWAYS)
public class HttpRequestExecuteAsyncMethodInnerClassConstructorInterceptor implements SimpleAroundInterceptor, HttpClientConstants {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final MetadataAccessor asyncTraceIdAccessor;
    private InterceptorGroup interceptorGroup;
    
    public HttpRequestExecuteAsyncMethodInnerClassConstructorInterceptor(TraceContext traceContext, MethodDescriptor descriptor, @Name(HttpClientConstants.METADATA_ASYNC_TRACE_ID) MetadataAccessor asyncTraceIdAccessor, InterceptorGroup interceptorGroup) {
        this.asyncTraceIdAccessor = asyncTraceIdAccessor;
        this.interceptorGroup = interceptorGroup;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        try {
            if (!validate(target, args)) {
                return;
            }

            InterceptorGroupInvocation transaction = interceptorGroup.getCurrentInvocation();
            if(transaction != null && transaction.getAttachment() != null) {
                AsyncTraceId asyncTraceId = (AsyncTraceId) transaction.getAttachment();
                asyncTraceIdAccessor.set(target, asyncTraceId);
                // clear.
                transaction.removeAttachment();
            }
        } catch (Throwable t) {
            logger.warn("Failed to before process. {}", t.getMessage(), t);
        }
    }

    private boolean validate(final Object target, final Object[] args) {
        if (!asyncTraceIdAccessor.isApplicable(target)) {
            logger.debug("Invalid target object. Need metadata accessor({}).", METADATA_ASYNC_TRACE_ID);
            return false;
        }

        return true;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }
    }
}