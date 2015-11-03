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
package com.navercorp.pinpoint.plugin.log4j.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor0;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.IgnoreMethod;
import org.apache.log4j.MDC;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetConstructor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetConstructors;

/**
 * @author minwoo.jung
 */

@TargetConstructors({
        @TargetConstructor({"java.lang.String", "org.apache.log4j.Category", "org.apache.log4j.Priority", "java.lang.Object", "java.lang.Throwable"}),
        @TargetConstructor({"java.lang.String", "org.apache.log4j.Category", "long", "org.apache.log4j.Priority", "java.lang.Object", "java.lang.Throwable"}),
        @TargetConstructor({"java.lang.String", "org.apache.log4j.Category", "long", "org.apache.log4j.Level", "java.lang.Object", "java.lang.String", "org.apache.log4j.spi.ThrowableInformation", "java.lang.String", "org.apache.log4j.spi.LocationInfo", "java.util.Map"})
})
public class LoggingEventOfLog4jInterceptor implements AroundInterceptor0 {
    private static final String TRANSACTION_ID = "PtxId";
    private static final String SPAN_ID = "PspanId";
    
    private final TraceContext traceContext;
    
    public LoggingEventOfLog4jInterceptor(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target) {
        Trace trace = traceContext.currentTraceObject();
        
        if (trace == null) {
            MDC.remove(TRANSACTION_ID);
            MDC.remove(SPAN_ID);
            return;
        } else {
            MDC.put(TRANSACTION_ID, trace.getTraceId().getTransactionId());
            MDC.put(SPAN_ID, String.valueOf(trace.getTraceId().getSpanId()));
        }
    }

    @IgnoreMethod
    @Override
    public void after(Object target, Object result, Throwable throwable) {

    }
}
