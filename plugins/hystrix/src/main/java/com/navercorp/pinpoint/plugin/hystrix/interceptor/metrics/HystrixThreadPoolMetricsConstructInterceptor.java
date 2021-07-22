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

package com.navercorp.pinpoint.plugin.hystrix.interceptor.metrics;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.plugin.hystrix.HystrixPluginConstants;
import com.navercorp.pinpoint.plugin.hystrix.descriptor.HystrixThreadPoolMetricsMethodDescriptor;
import com.navercorp.pinpoint.plugin.hystrix.field.HystrixKeyNameAccessor;

/**
 * @author HyunGil Jeong
 */
public class HystrixThreadPoolMetricsConstructInterceptor extends SpanEventSimpleAroundInterceptorForPlugin {

    private static final HystrixThreadPoolMetricsMethodDescriptor HYSTRIX_THREAD_POOL_METRICS_METHOD_DESCRIPTOR = new HystrixThreadPoolMetricsMethodDescriptor();

    public HystrixThreadPoolMetricsConstructInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        super(traceContext, descriptor);
        traceContext.cacheApi(HYSTRIX_THREAD_POOL_METRICS_METHOD_DESCRIPTOR);
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        // do nothing
    }

    @Override
    protected void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        recorder.recordServiceType(HystrixPluginConstants.HYSTRIX_INTERNAL_SERVICE_TYPE);
        recorder.recordApi(HYSTRIX_THREAD_POOL_METRICS_METHOD_DESCRIPTOR);
        recorder.recordException(throwable);
        if (ArrayUtils.hasLength(args)) {
            if (args[0] instanceof HystrixKeyNameAccessor) {
                String threadPoolKey = ((HystrixKeyNameAccessor) args[0])._$PINPOINT$_getHystrixKeyName();
                if (threadPoolKey != null) {
                    recorder.recordAttribute(HystrixPluginConstants.HYSTRIX_THREAD_POOL_KEY_ANNOTATION_KEY, threadPoolKey);
                }
            }
        }
    }
}
