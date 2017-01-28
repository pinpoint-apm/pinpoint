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
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.sampler.SamplingFlagUtils;
import com.navercorp.pinpoint.plugin.vertx.VertxConstants;
import io.vertx.core.http.HttpClientRequest;

/**
 * @author jaehong.kim
 */
public class HttpClientImplDoRequestInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor methodDescriptor;

    public HttpClientImplDoRequestInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.methodDescriptor = descriptor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        if (!trace.canSampled()) {
            return;
        }

        trace.traceBlockBegin();
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        HttpClientRequest request = null;
        if (validate(result)) {
            request = (HttpClientRequest) result;
        }

        if (!trace.canSampled()) {
            if (request != null) {
                request.putHeader(Header.HTTP_SAMPLED.toString(), SamplingFlagUtils.SAMPLING_RATE_FALSE);
            }
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(methodDescriptor);
            recorder.recordException(throwable);
            recorder.recordServiceType(VertxConstants.VERTX_HTTP_CLIENT_INTERNAL);

            if (request != null) {
                // make asynchronous trace-id
                final AsyncTraceId asyncTraceId = trace.getAsyncTraceId();
                recorder.recordNextAsyncId(asyncTraceId.getAsyncId());
                ((AsyncTraceIdAccessor) request)._$PINPOINT$_setAsyncTraceId(asyncTraceId);
                if (isDebug) {
                    logger.debug("Set asyncTraceId metadata {}", asyncTraceId);
                }
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    private boolean validate(final Object result) {
        if (result == null || !(result instanceof HttpClientRequest)) {
            if (isDebug) {
                logger.debug("Invalid result object. result={}.", result);
            }
            return false;
        }

        if (!(result instanceof AsyncTraceIdAccessor)) {
            if (isDebug) {
                logger.debug("Invalid result object. Need metadata accessor({}).", AsyncTraceIdAccessor.class.getName());
            }
            return false;
        }

        return true;
    }
}
