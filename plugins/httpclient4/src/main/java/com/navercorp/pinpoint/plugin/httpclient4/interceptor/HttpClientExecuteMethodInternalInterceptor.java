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

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupInvocation;
import com.navercorp.pinpoint.bootstrap.interceptor.http.HttpCallContext;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Group;
import com.navercorp.pinpoint.plugin.httpclient4.HttpClient4Constants;

import org.apache.http.*;

/**
 * Trace status code.
 * 
 * @author minwoo.jung
 * @author jaehong.kim
 */
@Group(value = HttpClient4Constants.HTTP_CLIENT4_SCOPE, executionPoint = ExecutionPolicy.INTERNAL)
public class HttpClientExecuteMethodInternalInterceptor implements SimpleAroundInterceptor, HttpClient4Constants {

    private boolean isHasCallbackParam;

    protected final PLogger logger;
    protected final boolean isDebug;

    protected TraceContext traceContext;
    private InterceptorGroup interceptorGroup;

    public HttpClientExecuteMethodInternalInterceptor(boolean isHasCallbackParam, TraceContext context, InterceptorGroup interceptorGroup) {
        this.logger = PLoggerFactory.getLogger(this.getClass());
        this.isDebug = logger.isDebugEnabled();

        this.traceContext = context;
        this.interceptorGroup = interceptorGroup;
        this.isHasCallbackParam = isHasCallbackParam;
    }

    @Override
    public void before(Object target, Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, target.getClass().getName(), "", "internal", args);
        }

        if (!needGetStatusCode()) {
            return;
        }

        if (result instanceof HttpResponse) {
            HttpResponse response = (HttpResponse) result;

            if (response.getStatusLine() != null) {
                HttpCallContext context = new HttpCallContext();
                final StatusLine statusLine = response.getStatusLine();
                if (statusLine != null) {
                    context.setStatusCode(statusLine.getStatusCode());
                    InterceptorGroupInvocation transaction = interceptorGroup.getCurrentInvocation();
                    transaction.setAttachment(context);
                }
            }
        }
    }

    private boolean needGetStatusCode() {
        if (isHasCallbackParam) {
            return false;
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return false;
        }

        // TODO fix me.
//        if (trace.getServiceType() != ServiceType.HTTP_CLIENT.getCode()) {
//            return false;
//        }

        InterceptorGroupInvocation transaction = interceptorGroup.getCurrentInvocation();
        if (transaction.getAttachment() != null) {
            return false;
        }

        return true;
    }
}
