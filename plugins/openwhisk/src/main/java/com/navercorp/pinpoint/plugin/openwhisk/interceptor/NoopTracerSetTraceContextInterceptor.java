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

import com.navercorp.pinpoint.bootstrap.async.AsyncContextAccessor;
import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.util.NumberUtils;
import com.navercorp.pinpoint.plugin.openwhisk.OpenwhiskConstants;
import com.navercorp.pinpoint.plugin.openwhisk.descriptor.DefaultMethodDescriptor;
import scala.collection.JavaConversions;

import java.util.Map;


/**
 * @author Seonghyun Oh
 */
public class NoopTracerSetTraceContextInterceptor implements AroundInterceptor {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    private static final DefaultMethodDescriptor METHOD_DESCRIPTOR = new DefaultMethodDescriptor("Openwhisk Entry Point");

    private final TraceContext traceContext;
    private final MethodDescriptor descriptor;


    public NoopTracerSetTraceContextInterceptor(TraceContext traceContext, MethodDescriptor descriptor) {
        this.traceContext = traceContext;
        this.descriptor = descriptor;
        traceContext.cacheApi(METHOD_DESCRIPTOR);
    }

    @Override
    public void before(Object target, Object[] args) {

        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        AsyncContext currentContext = ((AsyncContextAccessor) args[0])._$PINPOINT$_getAsyncContext();
        if (currentContext != null) {
            return;
        }

        if (((scala.Option)args[1]).isEmpty()) {
            return;
        }

        Trace trace = populateTraceId((scala.Option)args[1]);
        if (trace == null) {
            return;
        }

        try {
            final SpanEventRecorder recorder = trace.traceBlockBegin();
            recorder.recordServiceType(OpenwhiskConstants.OPENWHISK_INTERNAL);
            recorder.recordApi(METHOD_DESCRIPTOR);

            final AsyncContext asyncContext = recorder.recordNextAsyncContext();
            ((AsyncContextAccessor) args[0])._$PINPOINT$_setAsyncContext(asyncContext);

        } catch (Throwable t) {
            if (logger.isWarnEnabled()) {
                logger.warn("AFTER. Caused:{}", t.getMessage(), t);
            }
        } finally {
            trace.traceBlockEnd();
            deleteTrace(trace);
        }

    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
    }


    private Trace populateTraceId(scala.Option context) {

        scala.collection.Map traceContextMap = (scala.collection.Map)context.get();
        Map map = JavaConversions.mapAsJavaMap(traceContextMap);

        String transactionId = (String)map.get("transactionId");
        String spanId = (String)map.get("spanId");
        String parentSpanId = (String)map.get("parentSpanId");
        String flag = (String)map.get("flag");
        String applicationName = (String)map.get("applicationName");
        String serverTypeCode = (String)map.get("serverTypeCode");
        String entityPath = (String)map.get("entityPath");
        String endPoint = (String)map.get("endPoint");

        TraceId traceId = traceContext.createTraceId(
                transactionId,
                NumberUtils.parseLong(parentSpanId, SpanId.NULL),
                NumberUtils.parseLong(spanId, SpanId.NULL),
                NumberUtils.parseShort(flag, (short) 0)
        );

        if (traceId != null) {
            Trace trace = traceContext.continueAsyncTraceObject(traceId);

            final SpanRecorder recorder = trace.getSpanRecorder();
            recorder.recordServiceType(OpenwhiskConstants.OPENWHISK_INVOKER);
            recorder.recordApi(descriptor);

            recorder.recordAcceptorHost(endPoint);
            recorder.recordRpcName(entityPath);

            // Record parent application
            recorder.recordParentApplication(applicationName, Short.valueOf(serverTypeCode));
            return trace;
        }
        return null;
    }


    private void deleteTrace(final Trace trace) {
        traceContext.removeTraceObject();
        trace.close();
    }

}

