/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.netty.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.netty.NettyConstants;

/**
 * @author Taejin Koo
 */
public class ChannelPromiseAddListenerInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public ChannelPromiseAddListenerInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        recorder.recordServiceType(NettyConstants.SERVICE_TYPE);
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
        if (isAsynchronousInvocation(target, args, result, throwable)) {
            // set asynchronous trace
            final AsyncContext asyncContext = recorder.recordNextAsyncContext();

            ((AsyncContextAccessor) result)._$PINPOINT$_setAsyncContext(asyncContext);
            if (isDebug) {
                logger.debug("Set AsyncContext {}", asyncContext);
            }
        }
    }

    private boolean isAsynchronousInvocation(final Object target, final Object[] args, Object result, Throwable throwable) {
        if (throwable != null || result == null) {
            return false;
        }

        if (!(result instanceof AsyncContextAccessor)) {
            logger.debug("Invalid result object. Need accessor({}).", AsyncContextAccessor.class.getName());
            return false;
        }

        return true;
    }

}

