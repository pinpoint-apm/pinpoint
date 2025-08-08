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

import com.navercorp.pinpoint.bootstrap.config.HttpStatusCodeErrors;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventEndPointApiAwareInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.http.HttpStatusCodeRecorder;
import com.navercorp.pinpoint.plugin.vertx.VertxConstants;
import io.vertx.core.http.HttpServerResponse;

/**
 * @author jaehong.kim
 */
public class HttpServerResponseImplInterceptor extends AsyncContextSpanEventEndPointApiAwareInterceptor {

    private final HttpStatusCodeRecorder httpStatusCodeRecorder;

    public HttpServerResponseImplInterceptor(TraceContext traceContext) {
        super(traceContext);
        ProfilerConfig profilerConfig = traceContext.getProfilerConfig();
        this.httpStatusCodeRecorder = new HttpStatusCodeRecorder(HttpStatusCodeErrors.of(profilerConfig::readString));
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args) {
        recorder.recordServiceType(VertxConstants.VERTX_HTTP_SERVER_INTERNAL);
    }

    @Override
    public void afterTrace(AsyncContext asyncContext, Trace trace, SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        if (trace.canSampled()) {
            recorder.recordApiId(apiId);
            recorder.recordException(throwable);
        }

        if (target instanceof HttpServerResponse) {
            final HttpServerResponse response = (HttpServerResponse) target;
            final SpanRecorder spanRecorder = trace.getSpanRecorder();
            this.httpStatusCodeRecorder.record(spanRecorder, response.getStatusCode());
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
    }
}