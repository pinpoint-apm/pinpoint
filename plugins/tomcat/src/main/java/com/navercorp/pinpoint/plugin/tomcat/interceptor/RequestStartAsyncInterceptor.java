/**
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.plugin.tomcat.interceptor;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.plugin.tomcat.TomcatConstants;

/**
 * 
 * @author jaehong.kim
 *
 */
public class RequestStartAsyncInterceptor implements SimpleAroundInterceptor, TomcatConstants {

    private PLogger logger = PLoggerFactory.getLogger(this.getClass());
    private boolean isDebug = logger.isDebugEnabled();

    private TraceContext traceContext;
    private MethodDescriptor descriptor;
    private MetadataAccessor asyncAccessor;
    private MetadataAccessor asyncTraceIdAccessor;

    public RequestStartAsyncInterceptor(TraceContext context, MethodDescriptor descriptor, @Name(METADATA_ASYNC) MetadataAccessor asyncAccessor, @Name(METADATA_ASYNC_TRACE_ID) MetadataAccessor asyncTraceIdAccessor) {
        this.traceContext = context;
        setMethodDescriptor(descriptor);
        this.asyncAccessor = asyncAccessor;
        this.asyncTraceIdAccessor = asyncTraceIdAccessor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if(isDebug) {
            logger.beforeInterceptor(target, "", descriptor.getMethodName(), "", args);            
        }


        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        trace.traceBlockBegin();
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if(isDebug) {
            logger.afterInterceptor(target, "", descriptor.getMethodName(), "", args);            
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            if (validate(target, result, throwable)) {
                asyncAccessor.set(target, Boolean.TRUE);

                // make asynchronous trace-id
                final AsyncTraceId asyncTraceId = trace.getAsyncTraceId();
                recorder.recordNextAsyncId(asyncTraceId.getAsyncId());
                // result is BasicFuture
                asyncTraceIdAccessor.set(result, asyncTraceId);
                if(isDebug) {
                    logger.debug("Set asyncTraceId metadata {}", asyncTraceId);                    
                }
            }
            
            recorder.recordServiceType(TOMCAT_METHOD);
            recorder.recordApi(descriptor);
            recorder.recordException(throwable);
        } catch (Throwable t) {
            logger.warn("Failed to after process. {}", t.getMessage(), t);
        } finally {
            trace.traceBlockEnd();
        }
    }

    private boolean validate(final Object target, final Object result, final Throwable throwable) {
        if (throwable != null || result == null) {
            return false;
        }

        if (!asyncAccessor.isApplicable(target)) {
            logger.debug("Invalid target object. Need metadata accessor({}).", METADATA_ASYNC);
            return false;
        }

        if (!asyncTraceIdAccessor.isApplicable(result)) {
            logger.debug("Invalid target object. Need metadata accessor({}).", METADATA_ASYNC_TRACE_ID);
            return false;
        }

        return true;
    }
    
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        this.descriptor = descriptor;
        traceContext.cacheApi(descriptor);
    }
}