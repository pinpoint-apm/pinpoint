/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.jetty.jakarta.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.jetty.JettyConstants;
import com.navercorp.pinpoint.plugin.jetty.jakarta.JettyAsyncListener;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.http.HttpServletRequest;


/**
 * @author jaehong.kim
 */
public class RequestStartAsyncInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public RequestStartAsyncInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
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
            // Add async listener. Servlet 3.0
            final AsyncContext asyncContext = (AsyncContext) result;
            final AsyncListener asyncListener = new JettyAsyncListener(this.traceContext, recorder.recordNextAsyncContext(true));
            asyncContext.addListener(asyncListener);
            if (isDebug) {
                logger.debug("Add async listener {}", asyncListener);
            }
        }
        recorder.recordServiceType(JettyConstants.JETTY_METHOD);
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
    }

    private boolean validate(final Object target, final Object result, final Throwable throwable) {
        if (throwable != null || result == null) {
            return false;
        }

        if (!(target instanceof HttpServletRequest)) {
            logger.debug("Invalid target object. {}", target);
            return false;
        }


        if (!(result instanceof AsyncContext)) {
            logger.debug("Invalid result object. {}.", result);
            return false;
        }

        return true;
    }
}