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

package com.navercorp.pinpoint.plugin.user.interceptor;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.user.UserIncludeMethodDescriptor;

/**
 * @author jaehong.kim
 */
public class UserIncludeMethodInterceptor implements AroundInterceptor {
    // scope name, must be unique.
    private static final String SCOPE_NAME = "##USER_INCLUDE_TRACE";
    private static final UserIncludeMethodDescriptor USER_INCLUDE_METHOD_DESCRIPTOR = new UserIncludeMethodDescriptor();

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private TraceContext traceContext;
    private MethodDescriptor descriptor;
    private final String path;

    public UserIncludeMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        this.traceContext = traceContext;
        this.descriptor = methodDescriptor;
        this.path = toPath(methodDescriptor);
        traceContext.cacheApi(USER_INCLUDE_METHOD_DESCRIPTOR);
    }

    private String toPath(MethodDescriptor methodDescriptor) {
        if (methodDescriptor == null) {
            return "/userInclude";
        }
        final StringBuilder path = new StringBuilder("/");
        if (StringUtils.hasLength(methodDescriptor.getClassName())) {
            path.append(methodDescriptor.getClassName()).append("/");
        }

        if (StringUtils.hasLength(methodDescriptor.getMethodName())) {
            path.append(methodDescriptor.getMethodName());
        }
        return path.toString().replace('.', '/');
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            if (isDebug) {
                logger.debug("Not found trace. Create user include trace.");
            }

            // create user include trace for standalone entry point.
            trace = createUserIncludeTrace();
            if (trace == null) {
                return;
            }
        } else {
            if (isDebug) {
                logger.debug("Found trace {}, sampled={}.", trace, trace.canSampled());
            }
        }

        // entry scope(default & disable trace).
        entryUserIncludeTraceScope(trace);

        // check sampled.
        if (!trace.canSampled()) {
            // skip
            return;
        }

        trace.traceBlockBegin();
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }

        final Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            return;
        }

        // leave scope(default & disable trace).
        if (!leaveUserIncludeTraceScope(trace)) {
            if (logger.isInfoEnabled()) {
                logger.info("Failed to leave scope of user include trace. trace={}, sampled={}", trace, trace.canSampled());
            }
            // delete unstable trace.
            deleteUserIncludeTrace(trace);
            return;
        }

        // check sampled.
        if (!trace.canSampled()) {
            if (isUserIncludeTraceDestination(trace)) {
                deleteUserIncludeTrace(trace);
            }
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            recorder.recordServiceType(ServiceType.INTERNAL_METHOD);
            recorder.recordException(throwable);
        } finally {
            trace.traceBlockEnd();
            if (isUserIncludeTraceDestination(trace)) {
                deleteUserIncludeTrace(trace);
            }
        }
    }

    private Trace createUserIncludeTrace() {
        final Trace trace = traceContext.newTraceObject();
        if (isDebug) {
            logger.debug("New user include trace {} and sampled {}", trace, trace.canSampled());
        }
        // add user scope.
        TraceScope oldScope = trace.addScope(SCOPE_NAME);
        if (oldScope != null) {
            // delete corrupted trace.
            if (logger.isInfoEnabled()) {
                logger.info("Duplicated user include trace scope={}.", oldScope.getName());
            }
            deleteUserIncludeTrace(trace);
            return null;
        }

        if (trace.canSampled()) {
            // record root span.
            final SpanRecorder recorder = trace.getSpanRecorder();
            recorder.recordServiceType(ServiceType.STAND_ALONE);
            recorder.recordApi(USER_INCLUDE_METHOD_DESCRIPTOR);
            recorder.recordRemoteAddress("LOCAL");
            recorder.recordRpcName(this.path);
            recorder.recordEndPoint("/");
        }
        return trace;
    }

    private void deleteUserIncludeTrace(final Trace trace) {
        if (isDebug) {
            logger.debug("Delete user include trace={}, sampled={}", trace, trace.canSampled());
        }
        traceContext.removeTraceObject();
        trace.close();
    }

    private void entryUserIncludeTraceScope(final Trace trace) {
        final TraceScope scope = trace.getScope(SCOPE_NAME);
        if (scope != null) {
            scope.tryEnter();
            if (isDebug) {
                logger.debug("Try enter trace scope={}", scope.getName());
            }
        }
    }

    private boolean leaveUserIncludeTraceScope(final Trace trace) {
        final TraceScope scope = trace.getScope(SCOPE_NAME);
        if (scope != null) {
            if (scope.canLeave()) {
                scope.leave();
                if (isDebug) {
                    logger.debug("Leave trace scope={}", scope.getName());
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private boolean isUserIncludeTrace(final Trace trace) {
        final TraceScope scope = trace.getScope(SCOPE_NAME);
        return scope != null;
    }

    private boolean isUserIncludeTraceDestination(final Trace trace) {
        final TraceScope scope = trace.getScope(SCOPE_NAME);
        return scope != null && !scope.isActive();
    }
}