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
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.thrift.descriptor.ThriftAsyncClientMethodDescriptor;

/**
 * @author HyunGil Jeong
 */
public class TAsyncMethodCallStartInterceptor extends TAsyncMethodCallInternalMethodInterceptor {

    private final ThriftAsyncClientMethodDescriptor thriftAsyncClientMethodDescriptor = new ThriftAsyncClientMethodDescriptor();

    private final MetadataAccessor asyncTraceIdAccessor;
   
    public TAsyncMethodCallStartInterceptor(
            TraceContext traceContext,
            MethodDescriptor methodDescriptor,
            @Name(METADATA_ASYNC_MARKER) MetadataAccessor asyncMarkerAccessor,
            @Name(METADATA_ASYNC_TRACE_ID) MetadataAccessor asyncTraceIdAccessor) {
        super(traceContext, methodDescriptor, asyncMarkerAccessor);
        this.asyncTraceIdAccessor = asyncTraceIdAccessor;
        this.traceContext.cacheApi(this.thriftAsyncClientMethodDescriptor);
    }
    
    @Override
    public void before(Object target, Object[] args) {
        if (!validate0(target)) {
            return;
        }

        // Retrieve asyncTraceId
        final AsyncTraceId asyncTraceId = asyncTraceIdAccessor.get(target);
        Trace trace = traceContext.currentTraceObject();
        // Check if the method was invoked by the same thread or not, probabaly safe not to check but just in case.
        if (trace == null) {
            // another thread has invoked the method
            trace = traceContext.continueAsyncTraceObject(asyncTraceId, asyncTraceId.getAsyncId(), asyncTraceId.getSpanStartTime());
            if (trace == null) {
                logger.warn("Failed to continue async trace. 'result is null'");
                return;
            }
            SpanEventRecorder recorder = trace.currentSpanEventRecorder();
            recorder.recordServiceType(ServiceType.ASYNC);
            recorder.recordApi(this.thriftAsyncClientMethodDescriptor);
            super.asyncMarkerAccessor.set(target, Boolean.TRUE);
        }
        super.before(target, args);
    }

    private boolean validate0(Object target) {
        if (!this.asyncTraceIdAccessor.isApplicable(target) || !(this.asyncTraceIdAccessor.get(target) instanceof AsyncTraceId)) {
            if (isDebug) {
                logger.debug("Asynchronous invocation metadata not found");
            }
            return false;
        }
        if (!this.asyncMarkerAccessor.isApplicable(target)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need metadata accessor({})", METADATA_ASYNC_MARKER);
            }
            return false;
        }
        return true;
    }

}
