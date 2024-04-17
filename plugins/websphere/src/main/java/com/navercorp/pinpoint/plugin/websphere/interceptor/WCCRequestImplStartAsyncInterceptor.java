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

package com.navercorp.pinpoint.plugin.websphere.interceptor;

import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventApiIdAwareAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.websphere.WebsphereAsyncListener;
import com.navercorp.pinpoint.plugin.websphere.WebsphereConstants;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncListener;
import javax.servlet.http.HttpServletRequest;

/**
 * @author jaehong.kim
 */
public class WCCRequestImplStartAsyncInterceptor extends SpanEventApiIdAwareAroundInterceptorForPlugin {

    public WCCRequestImplStartAsyncInterceptor(TraceContext context) {
        super(context);
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args) {
    }

    @Override
    public Trace currentTrace() {
        return traceContext.currentRawTraceObject();
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        if (validate(target, result, throwable)) {
            final AsyncContext asyncContext = (AsyncContext) result;
            final AsyncListener asyncListener = new WebsphereAsyncListener(this.traceContext, recorder.recordNextAsyncContext(true));
            asyncContext.addListener(asyncListener);
            if (isDebug) {
                logger.debug("Add async listener {}", asyncListener);
            }
        }
        recorder.recordServiceType(WebsphereConstants.WEBSPHERE_METHOD);
        recorder.recordApiId(apiId);
        recorder.recordException(throwable);
    }

    private boolean validate(final Object target, final Object result, final Throwable throwable) {
        if (throwable != null || result == null) {
            return false;
        }

        if (!(target instanceof HttpServletRequest)) {
            logger.debug("Invalid target object, Not implemented of javax.servlet.http.HttpServletRequest. target={}", target);
            return false;
        }

        if (!(result instanceof AsyncContext)) {
            logger.debug("Invalid result object, Not implemented of javax.servlet.AsyncContext. result={}.", result);
            return false;
        }

        return true;
    }
}