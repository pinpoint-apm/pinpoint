/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.bootstrap.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.AsyncContextUtils;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogManager;
import com.navercorp.pinpoint.bootstrap.logging.PluginLogger;

import java.util.Objects;

/**
 * @author jaehong.kim
 */
public abstract class AbstractAsyncContextSpanEventEndPointInterceptor {
    protected final PluginLogger logger = PluginLogManager.getLogger(getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    protected final TraceContext traceContext;

    public AbstractAsyncContextSpanEventEndPointInterceptor(TraceContext traceContext) {
        this.traceContext = Objects.requireNonNull(traceContext, "traceContext");
    }

    protected AsyncContext getAsyncContext(Object target, Object[] args) {
        return AsyncContextAccessorUtils.getAsyncContext(target);
    }

    protected AsyncContext getAsyncContext(Object target, Object[] args, Object result, Throwable throwable) {
        return AsyncContextAccessorUtils.getAsyncContext(target);
    }

    protected Trace getAsyncTrace(AsyncContext asyncContext) {
        final Trace trace = asyncContext.continueAsyncTraceObject();
        if (trace == null) {
            return null;
        }

        return trace;
    }

    protected void deleteAsyncTrace(final Trace trace) {
        traceContext.removeTraceObject();
        trace.close();
    }


    protected void finishAsyncState(final AsyncContext asyncContext) {
        if (AsyncContextUtils.asyncStateFinish(asyncContext)) {
            if (isDebug) {
                logger.debug("finished asyncState. asyncTraceId={}", asyncContext);
            }
        }
    }
}