/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.spring.r2dbc.interceptor;

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventResultReplaceBlockSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.reactorsupport.SeamPublisherWrapper;
import com.navercorp.pinpoint.plugin.spring.r2dbc.SpringDataR2dbcConstants;

/**
 * Wrapping variant of {@link ConnectionFactoryCreateInterceptor} (config-gated by
 * {@code profiler.spring.data.r2dbc.wrap.publisher}): the connection publisher returned from
 * {@code ConnectionFactory.create()} is replaced with a wrapped one instead of receiving the
 * AsyncContext through its injected accessor field.
 */
public class WrappingConnectionFactoryCreateInterceptor extends SpanEventResultReplaceBlockSimpleAroundInterceptorForPlugin {

    public WrappingConnectionFactoryCreateInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(SpringDataR2dbcConstants.SPRING_DATA_R2DBC);
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordException(throwable);
        recorder.recordApi(methodDescriptor);
    }

    @Override
    protected Object replaceResult(SpanEventRecorder recorder, Object target, Class<?> returnType, Object[] args, Object result, Throwable throwable) {
        if (throwable != null || !SeamPublisherWrapper.isWrappable(result)) {
            return result;
        }

        final AsyncContext asyncContext = recorder.recordNextAsyncContext();
        final Object wrapped = SeamPublisherWrapper.wrap(result, asyncContext);
        if (isDebug) {
            logger.debug("Wrapped connection publisher. asyncContext={}", asyncContext);
        }
        return wrapped;
    }
}
