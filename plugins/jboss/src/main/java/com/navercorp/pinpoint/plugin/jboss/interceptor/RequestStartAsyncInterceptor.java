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

package com.navercorp.pinpoint.plugin.jboss.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.jboss.JbossAsyncListener;
import com.navercorp.pinpoint.plugin.jboss.JbossConstants;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;

/**
 * @author jaehong.kim
 */
public class RequestStartAsyncInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public RequestStartAsyncInterceptor(TraceContext context, MethodDescriptor descriptor) {
        super(context, descriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
    }

    @Override
    protected Trace currentTrace() {
        return traceContext.currentRawTraceObject();
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        if (validate(target, result, throwable)) {
            // Add async listener. Servlet 3.0
            final AsyncContext asyncContext = (AsyncContext) result;
            final AsyncListener asyncListener = new JbossAsyncListener(this.traceContext, recorder.recordNextAsyncContext(true));
            asyncContext.addListener(asyncListener);
            if (isDebug) {
                logger.debug("Add async listener {}", asyncListener);
            }
        }

        recorder.recordServiceType(JbossConstants.JBOSS_METHOD);
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
    }

    private boolean validate(final Object target, final Object result, final Throwable throwable) {
        if (throwable != null || result == null) {
            return false;
        }

        if (!(target instanceof HttpServletRequest)) {
            if (isDebug) {
                logger.debug("Invalid target object, The javax.servlet.http.HttpServletRequest interface is not implemented. target={}", target);
            }
            return false;
        }
        if (!(result instanceof AsyncContext)) {
            if (isDebug) {
                logger.debug("Invalid result object, The javax.servlet.AsyncContext interface is not implemented. result={}.", result);
            }
            return false;
        }
        return true;
    }

}