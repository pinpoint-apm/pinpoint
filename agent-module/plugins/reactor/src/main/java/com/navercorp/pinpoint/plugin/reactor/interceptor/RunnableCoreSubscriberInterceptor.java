/*
 * Copyright 2022 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.reactor.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AsyncContextSpanEventApiIdAwareAroundInterceptor;
import com.navercorp.pinpoint.plugin.reactor.ReactorConstants;

public class RunnableCoreSubscriberInterceptor extends AsyncContextSpanEventApiIdAwareAroundInterceptor {

    public RunnableCoreSubscriberInterceptor(TraceContext traceContext) {
        super(traceContext);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, AsyncContext asyncContext, Object target, int apiId, Object[] args) {
        final AsyncContext publisherAsyncContext = AsyncContextAccessorUtils.getAsyncContext(target);
        if (publisherAsyncContext != null) {
            AsyncContextAccessorUtils.setAsyncContext(publisherAsyncContext, args, 0);
            if (isDebug) {
                logger.debug("Set asyncContext to target. asyncContext={}", publisherAsyncContext);
            }
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) {
        recorder.recordApiId(apiId);
        recorder.recordServiceType(ReactorConstants.REACTOR);
        recorder.recordException(throwable);
    }
}