/*
 * Copyright 2016 Pinpoint contributors and NAVER Corp.
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

package com.navercorp.pinpoint.plugin.loggingevent.interceptor;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor0;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;

import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.IgnoreMethod;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.LoggingInfo;

/**
 * Created by Administrator on 2017/9/4.
 */
public class AppenderInterceptor implements AroundInterceptor0 {

    private final TraceContext traceContext;
    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    public AppenderInterceptor(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public void before(Object target) {
        logger.info("AppenderInterceptor.before is triggered...");
        Trace trace = traceContext.currentTraceObject();

        if (trace != null) {
            logger.info("AppenderInterceptor.before drip into trace");
            SpanRecorder recorder = trace.getSpanRecorder();
            recorder.recordLogging(LoggingInfo.LOGGED);
        }
    }

    @IgnoreMethod
    @Override
    public void after(Object target, Object result, Throwable throwable) {

    }
}