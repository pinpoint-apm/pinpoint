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

package com.navercorp.pinpoint.plugin.redis.interceptor;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptorForPlugin;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.plugin.redis.RedisConstants;

/**
 * Jedis (redis client) method interceptor
 * 
 * @author jaehong.kim
 *
 */
public class JedisMethodInterceptor extends SpanEventSimpleAroundInterceptorForPlugin implements RedisConstants {

    private MetadataAccessor endPointAccessor;

    public JedisMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, @Name(METADATA_END_POINT) MetadataAccessor endPointAccessor) {
        super(traceContext, methodDescriptor);

        this.endPointAccessor = endPointAccessor;
    }

    @Override
    public void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
    }

    @Override
    public void doInAfterTrace(SpanEventRecorder recorder, Object target, Object[] args, Object result, Throwable throwable) {
        String endPoint = null;

        if (endPointAccessor.isApplicable(target)) {
            endPoint = endPointAccessor.get(target);
        }

        recorder.recordApi(getMethodDescriptor());
        recorder.recordEndPoint(endPoint != null ? endPoint : "Unknown");
        recorder.recordDestinationId(REDIS.getName());
        recorder.recordServiceType(REDIS);
        recorder.recordException(throwable);
    }
}