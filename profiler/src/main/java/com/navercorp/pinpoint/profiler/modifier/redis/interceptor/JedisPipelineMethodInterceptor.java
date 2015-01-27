/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.modifier.redis.interceptor;

import java.util.Map;

import com.navercorp.pinpoint.bootstrap.context.RecordableTrace;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.TargetClassLoader;
import com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.MapTraceValue;
import com.navercorp.pinpoint.common.ServiceType;

/**
 * Jedis Pipeline(redis client) method interceptor
 * 
 * @author jaehong.kim
 *
 */
public class JedisPipelineMethodInterceptor extends SpanEventSimpleAroundInterceptor implements TargetClassLoader {

    public JedisPipelineMethodInterceptor() {
        super(JedisPipelineMethodInterceptor.class);
    }

    @Override
    public void doInBeforeTrace(RecordableTrace trace, Object target, Object[] args) {
        trace.markBeforeTime();
    }

    @Override
    public void doInAfterTrace(RecordableTrace trace, Object target, Object[] args, Object result, Throwable throwable) {
        String endPoint = null;
        if (target instanceof MapTraceValue) {
            final Map<String, Object> traceValue = ((MapTraceValue) target)._$PINPOINT$_getTraceBindValue();
            if (traceValue != null) {
                endPoint = (String) traceValue.get("endPoint");
            }
        }

        trace.recordApi(getMethodDescriptor());
        trace.recordEndPoint(endPoint != null ? endPoint : "Unknown");
        trace.recordDestinationId(ServiceType.REDIS.toString());
        trace.recordServiceType(ServiceType.REDIS);
        trace.recordException(throwable);
        trace.markAfterTime();
    }
}