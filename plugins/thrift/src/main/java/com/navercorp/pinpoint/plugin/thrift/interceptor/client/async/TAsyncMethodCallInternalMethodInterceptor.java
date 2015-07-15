/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.plugin.thrift.interceptor.client.async;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.thrift.ThriftConstants;

/**
 * @author HyunGil Jeong
 */
public class TAsyncMethodCallInternalMethodInterceptor implements SimpleAroundInterceptor, ThriftConstants {
    protected final PLogger logger = PLoggerFactory.getLogger(getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    protected final TraceContext traceContext;
    protected final MethodDescriptor methodDescriptor;
    protected final MetadataAccessor asyncMarkerAccessor;
    
    public TAsyncMethodCallInternalMethodInterceptor(
            TraceContext traceContext,
            MethodDescriptor methodDescriptor,
            @Name(METADATA_ASYNC_MARKER) MetadataAccessor asyncMarkerAccessor) {
        this.traceContext = traceContext;
        this.methodDescriptor = methodDescriptor;
        this.asyncMarkerAccessor = asyncMarkerAccessor;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (isDebug) {
            logger.beforeInterceptor(target, args);
        }
        
        if (!validate(target)) {
            return;
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }
        try {
            trace.traceBlockBegin();
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            doInBeforeTrace(recorder, target, args);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("before. Caused:{}", th.getMessage(), th);
            }
        }
    }
    
    protected void doInBeforeTrace(SpanEventRecorder recorder, final Object target, final Object[] args) {
        recorder.recordServiceType(getServiceType());
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (isDebug) {
            logger.afterInterceptor(target, args, result, throwable);
        }
        
        if (!validate(target)) {
            return;
        }

        final Trace trace = traceContext.currentTraceObject();
        if (trace == null) {
            return;
        }

        try {
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            doInAfterTrace(recorder, target, args, result, throwable);
        } catch (Throwable th) {
            if (logger.isWarnEnabled()) {
                logger.warn("after error. Caused:{}", th.getMessage(), th);
            }
        } finally {
            trace.traceBlockEnd();
        }
    }
    
    protected void doInAfterTrace(SpanEventRecorder recorder, final Object target, final Object[] args, final Object result, Throwable throwable) {
        recorder.recordApi(this.methodDescriptor);
        recorder.recordException(throwable);
    }
    
    protected boolean validate(Object target) {
        if (!this.asyncMarkerAccessor.isApplicable(target)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need metadata accessor({})", METADATA_ASYNC_MARKER);
            }
            return false;
        }
        if (!(this.asyncMarkerAccessor.get(target) instanceof Boolean)) {
            if (isDebug) {
                logger.debug("Invalid value for metadata {}", METADATA_ASYNC_MARKER);
            }
            return false;
        }
        return true;
    }
    
    protected ServiceType getServiceType() {
        return THRIFT_CLIENT_INTERNAL;
    }

}
