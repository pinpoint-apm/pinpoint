/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.user.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.util.ScopeUtils;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.ArrayUtils;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class MQExternalClientHandlerInterceptor implements AroundInterceptor {
    protected final PLogger logger = PLoggerFactory.getLogger(getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    protected final MethodDescriptor methodDescriptor;

    public MQExternalClientHandlerInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        Objects.requireNonNull(traceContext, "traceContext");
        this.methodDescriptor = Objects.requireNonNull(methodDescriptor, "methodDescriptor");
    }

    @Override
    public void before(Object target, Object[] args) {
        final AsyncContext asyncContext = getAsyncContext(args);
        if (asyncContext == null) {
            return;
        }

        final Trace trace = getAsyncTrace(asyncContext);
        if (trace == null) {
            return;
        }

        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        // entry scope.
        ScopeUtils.entryAsyncTraceScope(trace);

        try {
            // trace event for default & async.
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(ServiceType.INTERNAL_METHOD);
            recorder.recordApi(methodDescriptor);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        final AsyncContext asyncContext = getAsyncContext(args);
        if (asyncContext == null) {
            return;
        }

        final Trace trace = asyncContext.currentAsyncTraceObject();
        if (trace == null) {
            return;
        }

        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        // leave scope.
        if (!ScopeUtils.leaveAsyncTraceScope(trace)) {
            if (logger.isWarnEnabled()) {
                logger.warn("Failed to leave scope of async trace {}.", trace);
            }
            // delete unstable trace.
            deleteAsyncContext(trace, asyncContext);
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            if (throwable != null) {
                recorder.recordException(throwable);
            }
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceBlockEnd();
            if (ScopeUtils.isAsyncTraceEndScope(trace)) {
                deleteAsyncContext(trace, asyncContext);
            }
        }
    }

    private AsyncContext getAsyncContext(Object[] args) {
        if (ArrayUtils.isEmpty(args)) {
            return null;
        }
        for (Object arg : args) {
            if (arg instanceof AsyncContextAccessor) {
                return AsyncContextAccessorUtils.getAsyncContext(arg);
            }
        }
        return null;
    }

    private Trace getAsyncTrace(AsyncContext asyncContext) {
        final Trace trace = asyncContext.continueAsyncTraceObject();
        if (trace == null) {
            if (isDebug) {
                logger.debug("Failed to continue async trace. 'result is null'");
            }
            return null;
        }
        if (isDebug) {
            logger.debug("getAsyncTrace() trace {}, asyncContext={}", trace, asyncContext);
        }

        return trace;
    }

    private void deleteAsyncContext(final Trace trace, AsyncContext asyncContext) {
        if (isDebug) {
            logger.debug("Delete async trace {}.", trace);
        }

        trace.close();
        asyncContext.close();
    }

}
