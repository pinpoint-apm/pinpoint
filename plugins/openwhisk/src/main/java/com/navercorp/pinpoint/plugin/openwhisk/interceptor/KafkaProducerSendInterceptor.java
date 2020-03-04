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
import com.navercorp.pinpoint.plugin.openwhisk.OpenwhiskConstants;
import com.navercorp.pinpoint.plugin.openwhisk.setter.TraceContextSetter;
import scala.collection.immutable.Map;
import org.apache.openwhisk.core.connector.ActivationMessage;


/**
 * @author Seonghyun Oh
 */
public class KafkaProducerSendInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;

    public KafkaProducerSendInterceptor(final TraceContext traceContext, final MethodDescriptor methodDescriptor) {
        this.traceContext = traceContext;
        this.descriptor = methodDescriptor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (!(args[1] instanceof ActivationMessage)) {
            logger.debug("It is not ActivationMessage");
            return;
        }

        ActivationMessage activationMessage = (ActivationMessage) args[1];

        AsyncContext asyncContext = AsyncContextAccessorUtils.getAsyncContext((activationMessage).transid());
        if (asyncContext == null) {
            logger.debug("Not found asynchronous invocation metadata");
            return;
        }

        final Trace trace = asyncContext.continueAsyncTraceObject();
        if (trace == null) {
            return;
        }

        final SpanEventRecorder recorder = trace.traceBlockBegin();

        TraceId nextId = trace.getTraceId().getNextTraceId();
        String applicationName = traceContext.getApplicationName();
        String serverTypeCode = Short.toString(traceContext.getServerTypeCode());
        String entityPath = String.valueOf((Object)activationMessage.action());
        String endPoint = args[0].toString();

        recorder.recordServiceType(OpenwhiskConstants.OPENWHISK_CLIENT);
        recorder.recordApi(descriptor);
        recorder.recordNextSpanId(nextId.getSpanId());
        recorder.recordEndPoint(endPoint);
        recorder.recordDestinationId(endPoint);

        Map<String,String> map = new Map.Map4<String,String>(
                "transactionId", nextId.getTransactionId(),
                "spanId", String.valueOf(nextId.getSpanId()),
                "parentSpanId", String.valueOf(nextId.getParentSpanId()),
                "flag", String.valueOf(nextId.getFlags())
        );
        Map<String, String> traceMetadata = new Map.Map4<String,String>(
                "applicationName", applicationName,
                "serverTypeCode", serverTypeCode,
                "entityPath", entityPath,
                "endPoint", endPoint
        );

        map = map.$plus$plus(traceMetadata.toSeq());
        ((TraceContextSetter)activationMessage)._$PINPOINT$_setTraceContext(scala.Option.apply(map));

        trace.traceBlockEnd();
        deleteTrace(trace);
        asyncContext.close();
    }

    private void deleteTrace(final Trace trace) {
        traceContext.removeTraceObject();
        trace.close();
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
    }
}

