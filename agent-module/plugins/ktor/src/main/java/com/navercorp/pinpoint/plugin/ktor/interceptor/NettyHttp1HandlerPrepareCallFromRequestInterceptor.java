/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.ktor.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventApiIdAwareAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.ktor.KtorConstants;

public class NettyHttp1HandlerPrepareCallFromRequestInterceptor extends SpanEventApiIdAwareAroundInterceptorForPlugin {
    public NettyHttp1HandlerPrepareCallFromRequestInterceptor(TraceContext traceContext) {
        super(traceContext);
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args) throws Exception {
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) throws Exception {
        recorder.recordServiceType(KtorConstants.KTOR_INTERNAL);
        recorder.recordException(throwable);
        recorder.recordApiId(apiId);

        if (throwable == null) {
            AsyncContext asyncContext = recorder.recordNextAsyncContext();
            AsyncContextAccessorUtils.setAsyncContext(asyncContext, result);
        }
    }
}
