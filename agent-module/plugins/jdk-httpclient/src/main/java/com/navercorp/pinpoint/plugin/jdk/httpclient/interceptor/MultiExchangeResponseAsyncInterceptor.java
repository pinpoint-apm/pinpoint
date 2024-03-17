/*
 * Copyright 2024 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.jdk.httpclient.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.jdk.httpclient.HttpRequestImplClientRequestAdaptor;
import com.navercorp.pinpoint.plugin.jdk.httpclient.HttpRequestImplGetter;
import com.navercorp.pinpoint.plugin.jdk.httpclient.JdkHttpClientConstants;
import jdk.internal.net.http.HttpRequestImpl;

public class MultiExchangeResponseAsyncInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public MultiExchangeResponseAsyncInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    @Override
    protected Trace currentTrace() {
        return traceContext.currentRawTraceObject();
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) throws Exception {
        if (target instanceof AsyncContextAccessor) {
            final AsyncContext asyncContext = recorder.recordNextAsyncContext();
            AsyncContextAccessorUtils.setAsyncContext(asyncContext, target);
            if (isDebug) {
                logger.debug("Set AsyncContext {}", asyncContext);
            }
        }
    }

    @Override
    public void afterTrace(Trace trace, SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        if (trace.canSampled()) {
            if (target instanceof HttpRequestImplGetter) {
                final HttpRequestImpl request = ((HttpRequestImplGetter) target)._$PINPOINT$_getCurrentreq();
                if (request == null) {
                    return;
                }
                final String host = HttpRequestImplClientRequestAdaptor.getHost(request);
                if (host != null) {
                    recorder.recordAttribute(AnnotationKey.HTTP_INTERNAL_DISPLAY, host);
                }
            }
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordServiceType(JdkHttpClientConstants.JDK_HTTP_CLIENT_INTERNAL);
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);

        final AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(target);
        if (asyncContext != null) {
            AsyncContextAccessorUtils.setAsyncContext(asyncContext, result);
        }
    }
}
