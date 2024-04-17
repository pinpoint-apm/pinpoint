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

package com.navercorp.pinpoint.bootstrap.interceptor;

import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;

/**
 * @author emeroad
 * @author jaehong.kim
 */
public abstract class SpanEventApiIdAwareAroundInterceptorForPlugin extends AbstractSpanEventInterceptorForPlugin implements ApiIdAwareAroundInterceptor {

    protected SpanEventApiIdAwareAroundInterceptorForPlugin(TraceContext traceContext) {
        super(traceContext);
    }

    @Override
    public void before(Object target, int apiId, Object[] args) {
        if (isDebug) {
            logBeforeInterceptor(target, args);
        }

        prepareBeforeTrace(target, apiId, args);

        final Trace trace = currentTrace();
        if (trace == null) {
            return;
        }
        
        try {
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            beforeTrace(trace, recorder, target, apiId, args);
            doInBeforeTrace(recorder, target, apiId, args);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("BEFORE. Caused:{}", th.getMessage(), th);
            }
        }
    }

    protected void prepareBeforeTrace(Object target, int apiId, Object[] args) {
    }

    protected void beforeTrace(Trace trace, SpanEventRecorder recorder, Object target, int apiId, Object[] args) {
    }

    protected abstract void doInBeforeTrace(final SpanEventRecorder recorder, final Object target, final int apiId, final Object[] args) throws Exception;

    @Override
    public void after(Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logAfterInterceptor(target, args, result, throwable);
        }

        prepareAfterTrace(target, apiId, args, result, throwable);

        final Trace trace = currentTrace();
        if (trace == null) {
            return;
        }
        try {
            final SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            afterTrace(trace, recorder, target, apiId, args, result, throwable);
            doInAfterTrace(recorder, target, apiId, args, result, throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER error. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }

    protected void prepareAfterTrace(Object target, int apiId, Object[] args, Object result, Throwable throwable) {
    }

    protected void afterTrace(final Trace trace, final SpanEventRecorder recorder, final Object target, final int apiId, final Object[] args, final Object result, final Throwable throwable) {
    }

    protected abstract void doInAfterTrace(final SpanEventRecorder recorder, final Object target, final int apiId, final Object[] args, final Object result, Throwable throwable) throws Exception;
}