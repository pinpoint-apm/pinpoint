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
import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.plugin.openwhisk.OpenwhiskConfig;
import com.navercorp.pinpoint.plugin.openwhisk.OpenwhiskConstants;
import com.navercorp.pinpoint.plugin.openwhisk.descriptor.LogMarkerMethodDescriptor;
import scala.Function0;
import org.apache.openwhisk.common.LogMarkerToken;

/**
 * @author Seonghyun Oh
 */
public class TransactionIdMarkInterceptor implements AroundInterceptor {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());


    protected final boolean isDebug = logger.isDebugEnabled();
    private final boolean isLoggingMessage;

    public TransactionIdMarkInterceptor(TraceContext traceContext) {
        final OpenwhiskConfig config = new OpenwhiskConfig(traceContext.getProfilerConfig());
        this.isLoggingMessage = config.isLoggingMessage();
    }

    /**
     *
     * @param target
     * @param args TransactionMetadata, Identify, LogMarkerToken
     */
    @Override
    public void before(Object target, Object[] args) {

        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext(args, 0);
        if (asyncContext == null) {
            logger.debug("Not found asynchronous invocation metadata {}", (LogMarkerToken)args[2]);
            return;
        }

        Trace trace = asyncContext.continueAsyncTraceObject();
        if (trace == null) {
            logger.debug("trace object null");
            return;
        }

        final SpanEventRecorder recorder = trace.traceBlockBegin();
        recorder.recordServiceType(OpenwhiskConstants.OPENWHISK_INTERNAL);

        try {
            LogMarkerToken logMarkerToken = (LogMarkerToken) args[2];
            String message = ((Function0) args[3]).apply().toString();
            recorder.recordApi(new LogMarkerMethodDescriptor(logMarkerToken));

            if (isLoggingMessage && message.length() > 0) {
                recorder.recordAttribute(OpenwhiskConstants.MARKER_MESSAGE, message);
            }
        } finally {
            trace.traceBlockEnd();
            trace.close();
            asyncContext.close();
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
    }

}

