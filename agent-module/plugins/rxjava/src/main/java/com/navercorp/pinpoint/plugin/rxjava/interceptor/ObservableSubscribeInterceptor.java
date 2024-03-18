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

package com.navercorp.pinpoint.plugin.rxjava.interceptor;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.plugin.rxjava.RxJavaPluginConstants;

/**
 * @author HyunGil Jeong
 */
public class ObservableSubscribeInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    public ObservableSubscribeInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
    }

    @Override
    protected void logBeforeInterceptor(Object target, Object[] args) {
        // may be called from internal threads, which will clutter up logs
    }

    private void logBeforeInterceptor0(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        logBeforeInterceptor0(target, args);
    }

    @Override
    protected void logAfterInterceptor(Object target, Object[] args, Object result, Throwable throwable) {
        // may be called from internal threads, which will clutter up logs
    }

    private void logAfterInterceptor0(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        logAfterInterceptor0(target, args, result, throwable);
        recorder.recordServiceType(RxJavaPluginConstants.RX_JAVA);
        recorder.recordApi(methodDescriptor);
        recorder.recordException(throwable);
    }
}
