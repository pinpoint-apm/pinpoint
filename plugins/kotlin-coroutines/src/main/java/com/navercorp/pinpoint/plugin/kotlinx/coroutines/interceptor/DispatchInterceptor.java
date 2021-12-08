/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.kotlinx.coroutines.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.kotlinx.coroutines.CoroutinesConfig;
import com.navercorp.pinpoint.plugin.kotlinx.coroutines.CoroutinesConstants;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlinx.coroutines.CancellableContinuation;
import kotlinx.coroutines.CoroutineName;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class DispatchInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;
    private final List<String> includedNameList;

    public DispatchInterceptor(TraceContext traceContext, MethodDescriptor descriptor, CoroutinesConfig config) {
        this.traceContext = Objects.requireNonNull(traceContext, "traceContext");
        this.descriptor = Objects.requireNonNull(descriptor, "descriptor");

        Objects.requireNonNull(config, "config");
        this.includedNameList = config.getIncludedNameList();
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Continuation continuation = getContinuation(args);
        if (continuation == null) {
            return;
        }

        if (isCompletedContinuation(continuation)) {
            return;
        }

        if (!checkSupportCoroutinesName(continuation)) {
            return;
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        final SpanEventRecorder recorder = trace.traceBlockBegin();

        final AsyncContextAccessor asyncContextAccessor = getAsyncContextAccessor(args);
        if (asyncContextAccessor != null) {
            // make asynchronous trace-id
            final AsyncContext asyncContext = recorder.recordNextAsyncContext();
            asyncContextAccessor._$PINPOINT$_setAsyncContext(asyncContext);
        }
    }

    private Continuation getContinuation(final Object[] args) {
        if (ArrayUtils.getLength(args) == 2 && args[1] instanceof Continuation) {
            return (Continuation) args[1];
        }
        return null;
    }

    private boolean isCompletedContinuation(final Continuation continuation) {
        if (continuation instanceof CancellableContinuation) {
            return ((CancellableContinuation) continuation).isCompleted();
        }
        return false;
    }

    private boolean checkSupportCoroutinesName(final Continuation continuation) {
        final CoroutineContext.Key key = CoroutineName.Key;

        final CoroutineContext context = continuation.getContext();
        if (context != null) {
            Object element = context.get(key);
            if (element instanceof CoroutineName) {
                String name = ((CoroutineName) element).getName();
                if (name != null) {
                    return includedNameList.contains(name);
                }
            }
        }

        return false;
    }

    private AsyncContextAccessor getAsyncContextAccessor(final Object[] args) {
        if (ArrayUtils.getLength(args) != 2) {
            if (isDebug) {
                logger.debug("Invalid args object. args={}.", args);
            }
            return null;
        }

        if (args[1] instanceof AsyncContextAccessor) {
            return (AsyncContextAccessor) args[1];
        }

        if (isDebug) {
            logger.debug("Invalid args[1] object. Need metadata accessor({}).", AsyncContextAccessor.class.getName());
        }

        return null;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Continuation continuation = getContinuation(args);
        if (continuation == null) {
            return;
        }

        if (isCompletedContinuation(continuation)) {
            return;
        }

        if (!checkSupportCoroutinesName(continuation)) {
            return;
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            recorder.recordServiceType(CoroutinesConstants.SERVICE_TYPE);
            recorder.recordException(throwable);
        } finally {
            trace.traceBlockEnd();
        }
    }

}
