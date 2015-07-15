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

import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceType;
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
            if (!trace.canSampled()) {
                if(isDebug) {
                    logger.debug("New trace and can't sampled {}", trace);
                }
                return;
            } 
            if(isDebug) {
                logger.debug("New trace and sampled {}", trace);
            }
            SpanRecorder recorder = trace.getSpanRecorder();
            recordRootSpan(recorder);
        }

        trace.traceBlockBegin();
    }

    private void recordRootSpan(final SpanRecorder recorder) {
        // root
        recorder.recordServiceType(ServiceType.STAND_ALONE);
        recorder.recordApi(USER_INCLUDE_METHOD_DESCRIPTOR);
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
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApi(descriptor);
            recorder.recordServiceType(ServiceType.USER_INCLUDE);
            recorder.recordException(throwable);
        } finally {
            trace.traceBlockEnd();
            if(trace.getTraceType() == TraceType.USER && trace.isRootStack()) {
                trace.close();
                traceContext.removeTraceObject();
            }
        }
    }
}
