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

package com.navercorp.pinpoint.plugin.reactor.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.plugin.reactor.ReactorConstants;

/**
 * @author jaehong.kim
 */
public class CorePublisherInterceptor extends AsyncContextSpanEventSimpleAroundInterceptor {

    public CorePublisherInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, Object[] args) {
        final AsyncContext publisherAsyncContext = AsyncContextAccessorUtils.getAsyncContext(target);
        if (publisherAsyncContext != null && args != null && args.length >= 1) {
            // Set AsyncContext to CoreSubscriber
            if (args[0] instanceof AsyncContextAccessor) {
                ((AsyncContextAccessor) (args[0]))._$PINPOINT$_setAsyncContext(publisherAsyncContext);
                if(isDebug) {
                    logger.debug("Set AsyncContext args[0]={}", args[0]);
                }
            }
        }
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(ReactorConstants.REACTOR_NETTY);
        recorder.recordException(throwable);
    }
}