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

package com.navercorp.pinpoint.plugin.httpclient4.interceptor;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.ByteCodeMethodDescriptorSupport;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.Cached;
import com.navercorp.pinpoint.bootstrap.plugin.Name;
import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.plugin.httpclient4.HttpClient4Constants;

/**
 * 
 * @author netspider
 * @author jaehong.kim
 * 
 */
public class BasicFutureMethodInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, HttpClient4Constants {

    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private TraceContext traceContext;
    private MethodDescriptor descriptor;
    private MetadataAccessor asyncTraceIdAccessor;

    public BasicFutureMethodInterceptor(TraceContext traceContext, @Cached MethodDescriptor methodDescriptor, @Name(METADATA_ASYNC_TRACE_ID) MetadataAccessor asyncTraceIdAccessor) {
        this.traceContext = traceContext;
        this.descriptor = methodDescriptor;
        this.asyncTraceIdAccessor = asyncTraceIdAccessor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        if (!asyncTraceIdAccessor.isApplicable(target) || asyncTraceIdAccessor.get(target) == null) {
            logger.debug("Not found asynchronous invocation metadata");
            return;
        }

        final AsyncTraceId asyncTraceId = asyncTraceIdAccessor.get(target);
        boolean async = false;
        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            trace = traceContext.continueAsyncTraceObject(asyncTraceId, asyncTraceId.getAsyncId(), asyncTraceId.getSpanStartTime());
            if (trace == null) {
                logger.warn("Failed to continue async trace. 'result is null'");
                return;
            }
            async = true;
            if(isDebug) {
                logger.debug("Continue async trace {} [{}]", asyncTraceId, Thread.currentThread().getName());
            }
           
        }

        logger.debug("TraceBlockBegin [{}]", Thread.currentThread().getName());
        trace.traceBlockBegin();
        trace.markBeforeTime();
        trace.recordServiceType(ServiceType.HTTP_CLIENT_INTERNAL);
        if(async) {
            trace.recordAttribute(AnnotationKey.ASYNC, "");
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }

        Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            logger.debug("Not found trace");
            return;
        }

        try {
            trace.recordApi(descriptor);
            trace.recordException(throwable);
            trace.markAfterTime();
        } finally {
            trace.traceBlockEnd();
            if(trace.isAsync() && trace.isRootStack()) {
                trace.traceRootBlockEnd();
                traceContext.detachTraceObject();
                if(isDebug) {
                    logger.debug("End async trace {} [{}]", trace.getTraceId(), Thread.currentThread().getName());
                }
            }
        }
    }

    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        traceContext.cacheApi(descriptor);
    }
}