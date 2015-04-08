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
import com.navercorp.pinpoint.bootstrap.context.RecordableTrace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SpanEventSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Cached;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.plugin.redis.RedisConstants;

/**
 * Jedis (redis client) method interceptor
 * 
 * @author jaehong.kim
 *
 */
public class JedisMethodInterceptor extends SpanEventSimpleAroundInterceptor implements RedisConstants {

    private MetadataAccessor endPointAccessor;

    public JedisMethodInterceptor(TraceContext traceContext, @Cached MethodDescriptor methodDescriptor, @Name(METADATA_END_POINT) MetadataAccessor endPointAccessor) {
        super(JedisMethodInterceptor.class);

        this.endPointAccessor = endPointAccessor;

        setTraceContext(traceContext);
        setMethodDescriptor(methodDescriptor);
    }

    @Override
    public void doInBeforeTrace(RecordableTrace trace, Object target, Object[] args) {
        trace.markBeforeTime();
    }

    @Override
    public void doInAfterTrace(RecordableTrace trace, Object target, Object[] args, Object result, Throwable throwable) {
        String endPoint = null;

        if (endPointAccessor.isApplicable(target)) {
            endPoint = endPointAccessor.get(target);
        }

        trace.recordApi(getMethodDescriptor());
        trace.recordEndPoint(endPoint != null ? endPoint : "Unknown");
        trace.recordDestinationId(REDIS.getName());
        trace.recordServiceType(REDIS);
        trace.recordException(throwable);
        trace.markAfterTime();
    }
}