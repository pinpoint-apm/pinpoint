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

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventApiIdAwareAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.ReactorSubscriber;
import com.navercorp.pinpoint.bootstrap.plugin.reactor.ReactorSubscriberAccessorUtils;
import com.navercorp.pinpoint.plugin.reactor.ReactorConstants;
import com.navercorp.pinpoint.plugin.reactor.ReactorPluginConfig;

public class FluxAndMonoSubscribeMethodInterceptor extends SpanEventApiIdAwareAroundInterceptorForPlugin {

    private final boolean traceSubscribe;

    public FluxAndMonoSubscribeMethodInterceptor(TraceContext traceContext) {
        super(traceContext);
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
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args) throws Exception {
        ReactorSubscriber reactorSubscriber = ReactorSubscriberAccessorUtils.get(args, 0);
        if (reactorSubscriber == null) {
            final AsyncContext nextAsyncContext = recorder.recordNextAsyncContext();
            // set reactorSubscriber to args[0]
            reactorSubscriber = new ReactorSubscriber(nextAsyncContext);
            ReactorSubscriberAccessorUtils.set(reactorSubscriber, args, 0);
            if (isDebug) {
                logger.debug("Set reactorSubscriber to args[0]. reactorSubscriber={}", reactorSubscriber);
            }
        } else {
            if (isDebug) {
                logger.debug("Already set reactorSubscriber to args[0]. reactorSubscriber={}", reactorSubscriber);
            }
        }
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, int apiId, Object[] args, Object result, Throwable throwable) throws Exception {
        recorder.recordApiId(apiId);
        recorder.recordServiceType(ReactorConstants.REACTOR);
        recorder.recordException(throwable);
    }
}