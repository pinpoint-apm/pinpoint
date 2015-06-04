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

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.user.UserIncludeMethodDescriptor;

/**
 * @author jaehong.kim
 */
public class UserIncludeMethodInterceptor implements SimpleAroundInterceptor {
    private static final UserIncludeMethodDescriptor USER_INCLUDE_METHOD_DESCRIPTOR = new UserIncludeMethodDescriptor();
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private TraceContext traceContext;
    private MethodDescriptor descriptor;
    private boolean span = false;

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

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            trace = traceContext.newTraceObject();
            span = true;
            if (!trace.canSampled()) {
                if(isDebug) {
                    logger.debug("New trace and can't sampled {}", trace);
                }
                return;
            } 
            if(isDebug) {
                logger.debug("New trace and sampled {}", trace);
            }
            recordRootSpan(trace);
        }

        trace.traceBlockBegin();
        trace.markBeforeTime();
    }

    private void recordRootSpan(final Trace trace) {
        // root
        trace.markBeforeTime();
        trace.recordServiceType(ServiceType.STAND_ALONE);
        trace.recordApi(USER_INCLUDE_METHOD_DESCRIPTOR);
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

        try {
            trace.recordApi(descriptor);
            trace.recordServiceType(ServiceType.STAND_ALONE);
            trace.recordException(throwable);
            trace.markAfterTime();
        } finally {
            trace.traceBlockEnd();
            if(span) {
                trace.markAfterTime();
                trace.close();
                traceContext.removeTraceObject();
            }
        }
    }
}
