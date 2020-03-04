/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.openwhisk.interceptor;

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessorUtils;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.openwhisk.accessor.PinpointTraceAccessor;
import scala.Function0;

/**
 * @author Seonghyun Oh
 */
public class TransactionIdFailedInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;

    public TransactionIdFailedInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
    }

    @Override
    public void before(Object target, Object[] args) {

        AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(args[2]);
        final Trace trace = ((PinpointTraceAccessor) (args[2]))._$PINPOINT$_getPinpointTrace();

        if (asyncContext == null || trace == null) {
            return;
        }

        // set error message
        String message = ((Function0) args[3]).apply().toString();
        SpanEventRecorder recorder = trace.currentSpanEventRecorder();

        recorder.recordException(new Throwable(message));
        logger.debug("Record failed message : {}", message);

        // close trace block and context
        trace.traceBlockEnd();
        trace.close();
        asyncContext.close();
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {

    }

}

