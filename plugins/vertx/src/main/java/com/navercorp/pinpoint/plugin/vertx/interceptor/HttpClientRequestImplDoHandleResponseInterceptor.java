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

import com.navercorp.pinpoint.bootstrap.async.AsyncTraceIdAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanAsyncEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.plugin.vertx.VertxConstants;
import com.navercorp.pinpoint.plugin.vertx.VertxHttpClientConfig;
import io.vertx.core.http.impl.HttpClientResponseImpl;

/**
 * @author jaehong.kim
 */
public class HttpClientRequestImplDoHandleResponseInterceptor extends SpanAsyncEventSimpleAroundInterceptor {

    private boolean statusCode;

    public HttpClientRequestImplDoHandleResponseInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);

        final VertxHttpClientConfig config = new VertxHttpClientConfig(traceContext.getProfilerConfig());
        this.statusCode = config.isStatusCode();
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncTraceId asyncTraceId, Object target, Object[] args) {
        if (!validate(args)) {
            return;
        }

        final HttpClientResponseImpl response = (HttpClientResponseImpl) args[0];
        if (statusCode) {
            recorder.recordAttribute(AnnotationKey.HTTP_STATUS_CODE, response.statusCode());
        }

        ((AsyncTraceIdAccessor) response)._$PINPOINT$_setAsyncTraceId(asyncTraceId);
    }

    private boolean validate(final Object[] args) {
        if (args == null || args.length < 1 || !(args[0] instanceof HttpClientResponseImpl)) {
            if (isDebug) {
                logger.debug("Invalid args[0] object. args={}.", args);
            }
            return false;
        }

        if (!(args[0] instanceof AsyncTraceIdAccessor)) {
            if (isDebug) {
                logger.debug("Invalid args[0] object. Need metadata accessor({}).", AsyncTraceIdAccessor.class.getName());
            }
            return false;
        }

        return true;
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(VertxConstants.VERTX_HTTP_CLIENT_INTERNAL);
        recorder.recordException(throwable);
    }
}