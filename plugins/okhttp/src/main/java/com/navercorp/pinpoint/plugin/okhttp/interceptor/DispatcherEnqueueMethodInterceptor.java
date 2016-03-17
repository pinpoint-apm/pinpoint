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
package com.navercorp.pinpoint.plugin.okhttp.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncTraceIdAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.okhttp.OkHttpConstants;

/**
 * 
 * @author jaehong.kim
 *
 */
public class DispatcherEnqueueMethodInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private TraceContext traceContext;
    private MethodDescriptor methodDescriptor;

    public DispatcherEnqueueMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        if(!validate(args)) {
            return;
        }

        SpanEventRecorder recorder = trace.traceBlockBegin();
        try {
            // set asynchronous trace
            final AsyncTraceId asyncTraceId = trace.getAsyncTraceId();
            recorder.recordNextAsyncId(asyncTraceId.getAsyncId());

            // set async id.
            // AsyncTraceIdAccessor typeCheck validate();
            ((AsyncTraceIdAccessor)args[0])._$PINPOINT$_setAsyncTraceId(asyncTraceId);
            if (isDebug) {
                logger.debug("Set asyncTraceId metadata {}", asyncTraceId);
            }
        } catch (Throwable t) {
            logger.warn("Failed to before process. {}", t.getMessage(), t);
        }
    }

    private boolean validate(Object[] args) {
        if (args == null || args.length < 1 || args[0] == null || !(args[0] instanceof AsyncTraceIdAccessor)) {
            logger.debug("Invalid args[0] object {}. Need field accessor({}).", args, AsyncTraceIdAccessor.class.getName());
            return false;
        }

        return true;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        if(!validate(args)) {
            return;
        }

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(methodDescriptor);
            recorder.recordServiceType(OkHttpConstants.OK_HTTP_CLIENT_INTERNAL);
            recorder.recordException(throwable);
        } finally {
            trace.traceBlockEnd();
        }
    }
}