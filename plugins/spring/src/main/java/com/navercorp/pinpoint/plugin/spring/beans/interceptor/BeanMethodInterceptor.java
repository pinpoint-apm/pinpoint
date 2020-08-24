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

package com.navercorp.pinpoint.plugin.spring.beans.interceptor;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.ApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.ExceptionStackAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.spring.beans.SpringBeansConstants;

/**
 * 
 * @author netspider
 * @author emeroad
 */
public class BeanMethodInterceptor extends ExceptionStackAroundInterceptor { //implements ApiIdAwareAroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(BeanMethodInterceptor.class);
    private final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final boolean markError;

    public BeanMethodInterceptor(TraceContext traceContext, boolean markError) {
        this.traceContext = traceContext;
        this.markError = markError;
    }

    public void before(Object target, int apiId, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        final SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(SpringBeansConstants.SERVICE_TYPE);
    }

    public void after(Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordApiId(apiId);

            ProfilerConfig config = traceContext.getProfilerConfig();
            Throwable spanThrowable = processThrowable(config, target, args, result, throwable);
            recorder.recordException(markError, spanThrowable);
        } finally {
            trace.traceBlockEnd();
        }
    }

    @Override public void before(Object target, Object[] args) {

    }

    @Override public void after(Object target, Object[] args, Object result, Throwable throwable) {

    }
}