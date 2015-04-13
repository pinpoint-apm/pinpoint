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

import org.apache.http.concurrent.BasicFuture;

import com.navercorp.pinpoint.bootstrap.FieldAccessor;
import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.plugin.httpclient4.HttpClient4Constants;

/**
 * 
 * @author jaehong.kim
 *
 */
public class DefaultClientExchangeHandlerImplStartMethodInterceptor implements SimpleAroundInterceptor, HttpClient4Constants {
    private final PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private TraceContext traceContext;
    private MethodDescriptor methodDescriptor;
    private MetadataAccessor asyncTraceIdAccessor;
    private FieldAccessor resultFutureAccessor;

    public DefaultClientExchangeHandlerImplStartMethodInterceptor(TraceContext traceContext, MethodDescriptor methodDescriptor, @Name(METADATA_ASYNC_TRACE_ID) MetadataAccessor asyncTraceIdAccessor, @Name(FIELD_RESULT_FUTURE) FieldAccessor resultFutureAccessor) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
        this.asyncTraceIdAccessor = asyncTraceIdAccessor;
        this.resultFutureAccessor = resultFutureAccessor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        trace.traceBlockBegin();
        trace.markBeforeTime();
        
        try {
            if (!validate(target, args)) {
                return;
            }

            final AsyncTraceId asyncTraceId = trace.getAsyncTraceId();
            trace.recordNextAsyncId(asyncTraceId.getAsyncId());
            final BasicFuture resultFuture = resultFutureAccessor.get(target);
            asyncTraceIdAccessor.set(resultFuture, asyncTraceId);
            if(isDebug) {
                logger.debug("Set asyncTraceId metadata {}", asyncTraceId);
            }
        } catch (Throwable t) {
            logger.warn("Failed to before process. {}", t.getMessage(), t);
        }
    }

    private boolean validate(final Object target, final Object[] args) {
        if (!resultFutureAccessor.isApplicable(target)) {
            logger.debug("Invalid target object. Need field accessor({}).", FIELD_RESULT_FUTURE);
            return false;
        }

        final BasicFuture resultFuture = resultFutureAccessor.get(target);
        if(resultFuture == null) {
            logger.debug("Invalid target object. resultFuture field is null.");
            return false;
        }
        
        if (!asyncTraceIdAccessor.isApplicable(resultFuture)) {
            logger.debug("Invalid resultFuture field object. Need metadata accessor({}).", METADATA_ASYNC_TRACE_ID);
            return false;
        }

        return true;
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args);
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        
        trace.recordServiceType(HTTP_CLIENT4_INTERNAL);
        trace.recordApi(methodDescriptor);
        trace.recordException(throwable);
        trace.markAfterTime();
        trace.traceBlockEnd();
    }
}
