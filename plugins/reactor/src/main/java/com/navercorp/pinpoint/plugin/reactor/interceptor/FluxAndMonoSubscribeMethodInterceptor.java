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
import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.ReactorContextAccessorUtils;
import com.navercorp.pinpoint.plugin.reactor.ReactorConstants;
import com.navercorp.pinpoint.plugin.reactor.ReactorPluginConfig;

public class FluxAndMonoSubscribeMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    private final boolean traceSubscribe;

    public FluxAndMonoSubscribeMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor) {
        super(traceContext, methodDescriptor);
        final ReactorPluginConfig config = new ReactorPluginConfig(traceContext.getProfilerConfig());
        this.traceSubscribe = config.isTraceSubscribe();
    }

    public Trace currentTrace() {
        if (traceSubscribe) {
            return traceContext.currentTraceObject();
        }
        return null;
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) throws Exception {
        if (AsyncContextAccessorUtils.getAsyncContext(target) != null) {
            return;
        }
        if (ReactorContextAccessorUtils.getAsyncContext(target) != null) {
            return;
        }

        if (traceSubscribe) {
            final AsyncContext asyncContext = recorder.recordNextAsyncContext();
            ReactorContextAccessorUtils.setAsyncContext(asyncContext, target);
            if (isDebug) {
                logger.debug("Set reactorContext to target. asyncContext={}", asyncContext);
            }
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) throws Exception {
        recorder.recordApi(methodDescriptor);
        recorder.recordServiceType(ReactorConstants.REACTOR);
        recorder.recordException(throwable);
    }
}
