/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author jaehong.kim
 */
public abstract class SpanRecursiveAroundInterceptor implements AroundInterceptor {
    protected final PLogger logger = PLoggerFactory.getLogger(getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    protected final MethodDescriptor methodDescriptor;
    protected final TraceContext traceContext;
    protected final String scopeName;

    protected SpanRecursiveAroundInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, final String scopeName) {
        this.traceContext = Assert.requireNonNull(traceContext, "traceContext");
        this.methodDescriptor = Assert.requireNonNull(methodDescriptor, "methodDescriptor");
        this.scopeName = Assert.requireNonNull(scopeName, "scopeName");
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (isSkipTrace()) {
            // Skip recursive invoked or duplicated span(entry point)
            return;
        }

        try {
            final Trace trace = createTrace(target, args);
            if (isDebug) {
                logger.debug("Created trace. trace={}", trace);
            }

            if (trace == null) {
                // Skip
                return;
            }

            // init entry point scope
            if (!initScope(trace)) {
                // Defense code
                deleteTrace(trace);
                return;
            }

            // entry point scope
            entryScope(trace);

            if (!trace.canSampled()) {
                return;
            }
            // ------------------------------------------------------
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            doInBeforeTrace(recorder, target, args);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    private boolean isSkipTrace() {
        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return false;
        }
        if (hasScope(trace)) {
            // Entry Scope
            entryScope(trace);
            if (isDebug) {
                logger.debug("Skip recursive invoked");
            }
        } else {
            if (isDebug) {
                logger.debug("Skip duplicated entry point");
            }
        }
        // Skip recursive invoke or duplicated entry point
        return true;
    }


    protected abstract void doInBeforeTrace(final SpanEventRecorder recorder, Object target, final Object[] args);

    protected abstract Trace createTrace(final Object target, final Object[] args);

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        if (!hasScope(trace)) {
            // Not in scope
            return;
        }

        if (!leaveScope(trace)) {
            // Defense code
            deleteTrace(trace);
            return;
        }

        if (!isEndScope(trace)) {
            // Ignored recursive call.
            return;
        }

        if (!trace.canSampled()) {
            deleteTrace(trace);
            return;
        }

        // ------------------------------------------------------
        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            doInAfterTrace(recorder, target, args, result, throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceBlockEnd();
            deleteTrace(trace);
        }
    }

    protected abstract void doInAfterTrace(final SpanEventRecorder recorder, final Object target, final Object[] args, final Object result, Throwable throwable);

    private boolean initScope(final Trace trace) {
        final TraceScope oldScope = trace.addScope(this.scopeName);
        if (oldScope != null) {
            // delete corrupted trace.
            if (logger.isInfoEnabled()) {
                logger.info("Duplicated trace scope={}.", oldScope.getName());
            }
            return false;
        }

        return true;
    }

    private void entryScope(final Trace trace) {
        final TraceScope scope = trace.getScope(this.scopeName);
        if (scope != null) {
            scope.tryEnter();
            if (isDebug) {
                logger.debug("Try enter trace scope={}", scope.getName());
            }
        }
    }

    private boolean leaveScope(final Trace trace) {
        final TraceScope scope = trace.getScope(this.scopeName);
        if (scope != null) {
            if (scope.canLeave()) {
                scope.leave();
                if (isDebug) {
                    logger.debug("Leave trace scope={}", scope.getName());
                }
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Failed to leave scope. trace={}", trace);
                }
                return false;
            }
        }
        return true;
    }

    private boolean hasScope(final Trace trace) {
        final TraceScope scope = trace.getScope(this.scopeName);
        return scope != null;
    }

    private boolean isEndScope(final Trace trace) {
        final TraceScope scope = trace.getScope(this.scopeName);
        return scope != null && !scope.isActive();
    }

    private void deleteTrace(final Trace trace) {
        traceContext.removeTraceObject();
        trace.close();
        if (isDebug) {
            logger.debug("Delete trace.");
        }
    }
}