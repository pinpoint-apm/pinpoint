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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * 
 * @author jaehong.kim
 *
 */
public class HttpRequestExecuteAsyncMethodInnerClassConstructorInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final InterceptorScope interceptorScope;

    public HttpRequestExecuteAsyncMethodInnerClassConstructorInterceptor(TraceContext traceContext, MethodDescriptor descriptor, InterceptorScope interceptorScope) {
        this.interceptorScope = interceptorScope;
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

            final InterceptorScopeInvocation transaction = interceptorScope.getCurrentInvocation();
            final AsyncContext asyncContext = getAsyncContext(transaction);
            if (asyncContext != null) {
                // type check validate();
                ((AsyncContextAccessor)target)._$PINPOINT$_setAsyncContext(asyncContext);
                // clear.
                transaction.removeAttachment();
            }
        } catch (Throwable t) {
            logger.warn("Failed to BEFORE process. {}", t.getMessage(), t);
        }
    }

    private AsyncContext getAsyncContext(InterceptorScopeInvocation currentInvocation) {
        final Object attachment = getAttachment(currentInvocation);
        if (attachment instanceof AsyncContext) {
            return (AsyncContext) attachment;
        }
        return null;
    }


    private Object getAttachment(InterceptorScopeInvocation invocation) {
        if (invocation == null) {
            return null;
        }
        return invocation.getAttachment();
    }

    private boolean validate(final Object target, final Object[] args) {
        if (!(target instanceof AsyncContextAccessor)) {
            logger.debug("Invalid target object. Need field accessor({}).", AsyncContextAccessor.class.getName());
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