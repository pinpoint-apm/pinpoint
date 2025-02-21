/*
 * Copyright 2023 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.plugin.reactor.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.util.StringUtils;
import com.navercorp.pinpoint.plugin.reactor.ReactorConstants;
import com.navercorp.pinpoint.plugin.reactor.ReactorPluginConfig;
import com.navercorp.pinpoint.plugin.reactor.TimeoutDescriptionGetter;

public class TimeoutMainSubscriberDoTimeoutInterceptor extends AsyncContextSpanEventApiIdAwareAroundInterceptor {

    private final boolean traceTimeout;

    public TimeoutMainSubscriberDoTimeoutInterceptor(TraceContext traceContext) {
        super(traceContext);
        final ReactorPluginConfig config = new ReactorPluginConfig(traceContext.getProfilerConfig());
        this.traceTimeout = config.isTraceTimeout();
    }

    public AsyncContext getAsyncContext(Object target, Object[] args) {
        if (traceTimeout) {
            return AsyncContextAccessorUtils.getAsyncContext(target);
        }
        return null;
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, int apiId, Object[] args) {
    }

    public AsyncContext getAsyncContext(Object target, Object[] args, Object result, Throwable throwable) {
        if (traceTimeout) {
            return AsyncContextAccessorUtils.getAsyncContext(target);
        }
        return null;
    }

    @Override
    public void afterTrace(AsyncContext asyncContext, Trace trace, SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        if (traceTimeout && trace.canSampled()) {
            recorder.recordApiId(apiId);
            recorder.recordServiceType(ReactorConstants.REACTOR);
            recorder.recordException(throwable);

            if (target instanceof TimeoutDescriptionGetter) {
                final String message = ((TimeoutDescriptionGetter) target)._$PINPOINT$_getTimeoutDescription();
                if (StringUtils.hasLength(message)) {
                    final String descriptionMessage = "TIMEOUT(" + StringUtils.abbreviate(message, 256) + ")";
                    recorder.recordAttribute(AnnotationKey.ARGS0, descriptionMessage);
                } else {
                    recorder.recordAttribute(AnnotationKey.ARGS0, "TIMEOUT");
                }
            }
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
    }
}
