/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.reactor.netty.interceptor;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventEndPointInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.http.HttpStatusCodeRecorder;
import com.navercorp.pinpoint.plugin.reactor.netty.ReactorNettyConstants;
import io.netty.handler.codec.http.HttpResponseStatus;
import reactor.netty.http.server.HttpServerResponse;

/**
 * @author jaehong.kim
 */
public class ChannelOperationsInterceptor extends AsyncContextSpanEventEndPointInterceptor {

    private final HttpStatusCodeRecorder httpStatusCodeRecorder;

    public ChannelOperationsInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
        final ProfilerConfig config = traceContext.getProfilerConfig();
        this.httpStatusCodeRecorder = new HttpStatusCodeRecorder(config.getHttpStatusCodeErrors());
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
    }

    @Override
    public void prepareAfter(AsyncContext asyncContext, Trace trace, SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        if (target instanceof HttpServerResponse) {
            final HttpServerResponse httpServerResponse = (HttpServerResponse) target;
            final int statusCode = getStatusCode(httpServerResponse);
            final SpanRecorder spanRecorder = trace.getSpanRecorder();
            httpStatusCodeRecorder.record(spanRecorder, statusCode);
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(ReactorNettyConstants.REACTOR_NETTY_INTERNAL);
        recorder.recordException(throwable);
    }

    private int getStatusCode(final HttpServerResponse response) {
        try {
            HttpResponseStatus status = response.status();
            if (status != null) {
                return status.code();
            }
        } catch (Exception ignored) {
        }
        return 0;
    }
}
