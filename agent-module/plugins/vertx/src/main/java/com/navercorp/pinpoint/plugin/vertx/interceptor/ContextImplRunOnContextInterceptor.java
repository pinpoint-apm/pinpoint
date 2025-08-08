/*
 * Copyright 2016 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.vertx.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventApiIdAwareAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.vertx.VertxConstants;

/**
 * @author jaehong.kim
 */
public class ContextImplRunOnContextInterceptor extends SpanEventApiIdAwareAroundInterceptorForPlugin {

    public ContextImplRunOnContextInterceptor(final TraceContext traceContext) {
        super(traceContext);
    }

    @Override
    public Trace currentTrace() {
        return traceContext.currentRawTraceObject();
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args) {
        recorder.recordServiceType(VertxConstants.VERTX_INTERNAL);
        if (validate(args)) {
            // make asynchronous trace-id
            final AsyncContext asyncContext = recorder.recordNextAsyncContext();
            ((AsyncContextAccessor) args[0])._$PINPOINT$_setAsyncContext(asyncContext);
            if (isDebug) {
                logger.debug("Set asyncContext to args[0]. asyncContext={}", asyncContext);
            }
        }
    }

    private boolean validate(final Object[] args) {
        if (ArrayUtils.isEmpty(args)) {
            return false;
        }

        if (!(args[0] instanceof AsyncContextAccessor)) {
            return false;
        }

        return true;
    }

    @Override
    public void afterTrace(Trace trace, SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        if (trace.canSampled()) {
            recorder.recordApiId(apiId);
            recorder.recordException(throwable);
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
    }
}