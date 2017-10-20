/*
 * Copyright 2016 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.vertx.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.plugin.http.HttpStatusCodeRecorder;
import com.navercorp.pinpoint.plugin.vertx.ResponseGetter;
import com.navercorp.pinpoint.plugin.vertx.VertxConstants;
import io.netty.handler.codec.http.HttpResponse;

/**
 * @author jaehong.kim
 */
public class HttpServerResponseImplInterceptor extends AsyncContextSpanEventEndPointInterceptor {

    private final HttpStatusCodeRecorder httpStatusCodeRecorder;

    public HttpServerResponseImplInterceptor(MethodDescriptor methodDescriptor, TraceContext traceContext) {
        super(traceContext, methodDescriptor);

        this.httpStatusCodeRecorder = new HttpStatusCodeRecorder(traceContext.getProfilerConfig().getHttpStatusCodeErrors());
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(VertxConstants.VERTX_HTTP_SERVER_INTERNAL);
        recorder.recordException(throwable);

        if (target instanceof ResponseGetter) {
            final HttpResponse response = ((ResponseGetter) target)._$PINPOINT$_getResponse();
            // TODO more simple.
            final AsyncContext asyncContext = getAsyncContext(target);
            if (asyncContext != null) {
                final Trace trace = asyncContext.currentAsyncTraceObject();
                if (trace != null) {
                    final SpanRecorder spanRecorder = trace.getSpanRecorder();
                    this.httpStatusCodeRecorder.record(spanRecorder, response.status().code());
                }
            }
        }
    }
}