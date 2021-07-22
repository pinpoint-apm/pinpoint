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

package com.navercorp.pinpoint.plugin.spring.webflux.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.spring.webflux.SpringWebFluxConstants;

/**
 * @author jaehong.kim
 */
public class ExchangeFunctionMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public ExchangeFunctionMethodInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        if (isAsync(args)) {
            // make asynchronous trace-id
            final AsyncContext asyncContext = recorder.recordNextAsyncContext();
            ((AsyncContextAccessor) args[0])._$PINPOINT$_setAsyncContext(asyncContext);
            if (isDebug) {
                logger.debug("Set closeable-AsyncContext {}", asyncContext);
            }
        }
    }

    private boolean isAsync(Object[] args) {
        if (ArrayUtils.isEmpty(args)) {
            return false;
        }
        if (!(args[0] instanceof AsyncContextAccessor)) {
            return false;
        }
        return true;
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(SpringWebFluxConstants.SPRING_WEBFLUX);
        recorder.recordException(throwable);

        if (isAsync(args) && isAsync(result)) {
            final AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(args[0]);
            if (asyncContext != null) {
                ((AsyncContextAccessor) result)._$PINPOINT$_setAsyncContext(asyncContext);
                if (isDebug) {
                    logger.debug("Set closeable-AsyncContext {}", asyncContext);
                }
            }
        }
    }

    private boolean isAsync(Object result) {
        if (result == null) {
            return false;
        }
        if (!(result instanceof AsyncContextAccessor)) {
            return false;
        }
        return true;
    }
}