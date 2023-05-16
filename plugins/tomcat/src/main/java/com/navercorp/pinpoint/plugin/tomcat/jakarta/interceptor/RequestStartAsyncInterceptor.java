/*
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
package com.navercorp.pinpoint.plugin.tomcat.jakarta.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.request.AsyncListenerInterceptorHelper;
import com.navercorp.pinpoint.plugin.tomcat.TomcatConstants;
import com.navercorp.pinpoint.plugin.tomcat.jakarta.TomcatAsyncListenerAdaptor;
import jakarta.servlet.AsyncContext;
import jakarta.servlet.http.HttpServletRequest;

/**
 * @author jaehong.kim
 */
public class RequestStartAsyncInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public RequestStartAsyncInterceptor(TraceContext context, MethodDescriptor descriptor) {
        super(context, descriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) throws Exception {
        
    }

    @Override
    protected Trace currentTrace() {
        return traceContext.currentRawTraceObject();
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) throws Exception {
        if (validate(target, result, throwable)) {
            com.navercorp.pinpoint.bootstrap.context.AsyncContext nextAsyncContext = recorder.recordNextAsyncContext(true);

            final AsyncListenerInterceptorHelper listenerInterceptor = new AsyncListenerInterceptorHelper(traceContext, nextAsyncContext);

            // Add async listener
            final AsyncContext asyncContext = (AsyncContext) result;
            asyncContext.addListener(new TomcatAsyncListenerAdaptor(listenerInterceptor));

            // Set AsyncContext, AsyncListener
            final HttpServletRequest request = (HttpServletRequest) target;
            request.setAttribute(com.navercorp.pinpoint.bootstrap.context.AsyncContext.class.getName(), nextAsyncContext);
            request.setAttribute(TomcatConstants.TOMCAT_SERVLET_REQUEST_TRACE, listenerInterceptor);
            if (isDebug) {
                logger.debug("Add async listener {}", listenerInterceptor);
            }
        }
        recorder.recordServiceType(TomcatConstants.TOMCAT_METHOD);
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
    }

    private boolean validate(final Object target, final Object result, final Throwable throwable) {
        if (throwable != null || result == null) {
            return false;
        }

        if (!(target instanceof HttpServletRequest)) {
            if (isDebug) {
                logger.debug("Invalid target object, The jakarta.servlet.http.HttpServletRequest interface is not implemented. target={}", target);
            }
            return false;
        }
        if (!(result instanceof AsyncContext)) {
            if (isDebug) {
                logger.debug("Invalid result object, The jakarta.servlet.AsyncContext interface is not implemented. result={}.", result);
            }
            return false;
        }
        return true;
    }
}