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
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.user.UserConstants;
import com.navercorp.pinpoint.plugin.user.UserIncludeMethodDescriptor;

/**
 * @author jaehong.kim
 */
public class UserIncludeMethodInterceptor implements AroundInterceptor {
    // must be unique.
    private static final String SCOPE_NAME = "##USER_INCLUDE";
    private static final UserIncludeMethodDescriptor USER_INCLUDE_METHOD_DESCRIPTOR = new UserIncludeMethodDescriptor();


    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private TraceContext traceContext;
    private MethodDescriptor descriptor;

    public UserIncludeMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        this.traceContext = traceContext;
        this.descriptor = methodDescriptor;

        traceContext.cacheApi(USER_INCLUDE_METHOD_DESCRIPTOR);
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        Trace trace = traceContext.currentRawTraceObject();
        if (trace == null) {
            // entry point.
            trace = traceContext.newTraceObject(TraceType.USER);
            if (isDebug) {
                logger.debug("New user trace {} and sampled {}", trace, trace.canSampled());
            }
            // add user scope.
            trace.addScope(SCOPE_NAME);

            if (trace.canSampled()) {
                // record root span.
                final SpanRecorder recorder = trace.getSpanRecorder();
                recorder.recordServiceType(ServiceType.STAND_ALONE);
                recorder.recordApi(USER_INCLUDE_METHOD_DESCRIPTOR);
            }
        }

        //
        final TraceScope scope = trace.getScope(SCOPE_NAME);
        if (scope != null) {
            scope.tryEnter();
        }

        if (!trace.canSampled()) {
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

        final TraceScope scope = trace.getScope(SCOPE_NAME);
        if (scope != null) {
            if (scope.canLeave()) {
                scope.leave();
            } else {
                if (logger.isInfoEnabled()) {
                    logger.info("Detected invalid scope={}. close and remove user trace={}, sampled={}", scope.getName(), trace, trace.canSampled());
                }
                trace.close();
                traceContext.removeTraceObject();
                return;
            }
        }

        if (!trace.canSampled()) {
            if (scope != null && !scope.isActive()) {
                if (isDebug) {
                    logger.debug("Remove user trace={}, sampled={}", trace, trace.canSampled());
                }
                traceContext.removeTraceObject();
            }

            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            recorder.recordServiceType(UserConstants.USER_INCLUDE);
            recorder.recordException(throwable);
        } finally {
            trace.traceBlockEnd();
            if (scope != null && !scope.isActive()) {
                if (isDebug) {
                    logger.debug("Close and remove user trace={}, sampled={}", trace, trace.canSampled());
                }
                trace.close();
                traceContext.removeTraceObject();
            }
        }
    }
}