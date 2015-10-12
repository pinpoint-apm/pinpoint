/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.thrift.interceptor.client.async;

import org.apache.thrift.async.TAsyncMethodCall;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.thrift.ThriftConstants;
import com.navercorp.pinpoint.plugin.thrift.field.accessor.AsyncMarkerFlagFieldAccessor;

/**
 * @author HyunGil Jeong
 */
public class TAsyncMethodCallInternalMethodInterceptor implements AroundInterceptor {
    protected final PLogger logger = PLoggerFactory.getLogger(getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    protected final TraceContext traceContext;
    protected final MethodDescriptor methodDescriptor;

    public TAsyncMethodCallInternalMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (!validate(target)) {
            return;
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        try {
            trace.traceBlockBegin();
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordServiceType(getServiceType());
            doInBeforeTrace(recorder, target, args);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    protected void doInBeforeTrace(SpanEventRecorder recorder, final Object target, final Object[] args) {
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        if (!validate0(target)) {
            return;
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(this.methodDescriptor);
            recorder.recordException(throwable);
            doInAfterTrace(recorder, target, args, result, throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    protected void doInAfterTrace(SpanEventRecorder recorder, final Object target, final Object[] args, final Object result, Throwable throwable) {
    }

    private boolean validate0(Object target) {
        if (!(target instanceof TAsyncMethodCall)) {
            return false;
        }
        if (!(target instanceof AsyncMarkerFlagFieldAccessor)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need field accessor({}).", AsyncMarkerFlagFieldAccessor.class.getName());
            }
            return false;
        }
        return validate(target);
    }

    protected boolean validate(Object target) {
        return true;
    }

    protected ServiceType getServiceType() {
        return ThriftConstants.THRIFT_CLIENT_INTERNAL;
    }

}
