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

import org.apache.thrift.async.TAsyncMethodCall;

import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.plugin.thrift.ThriftUtils;

/**
 * @author HyunGil Jeong
 */
public class TAsyncMethodCallDoWritingRequestBodyInterceptor extends TAsyncMethodCallInternalMethodInterceptor {

    private final MetadataAccessor asyncNextSpanIdAccessor;
    private final MetadataAccessor asyncCallRemoteAddressAccessor;
    private final MetadataAccessor asyncCallEndFlagAccessor;
    
    public TAsyncMethodCallDoWritingRequestBodyInterceptor(
            TraceContext traceContext,
            MethodDescriptor methodDescriptor, 
            @Name(METADATA_ASYNC_MARKER) MetadataAccessor asyncMarkerAccessor,
            @Name(METADATA_ASYNC_NEXT_SPAN_ID) MetadataAccessor asyncNextSpanIdAccessor,
            @Name(METADATA_ASYNC_CALL_REMOTE_ADDRESS) MetadataAccessor asyncCallRemoteAddressAccessor,
            @Name(METADATA_ASYNC_CALL_END_FLAG) MetadataAccessor asyncCallEndFlagAccessor) {
        super(traceContext, methodDescriptor, asyncMarkerAccessor);
        this.asyncNextSpanIdAccessor = asyncNextSpanIdAccessor;
        this.asyncCallRemoteAddressAccessor = asyncCallRemoteAddressAccessor;
        this.asyncCallEndFlagAccessor = asyncCallEndFlagAccessor;
    }

    @Override
    protected void doInBeforeTrace(SpanEventRecorder recorder, Object target, Object[] args) {
        super.doInBeforeTrace(recorder, target, args);
        
        Long nextSpanId = this.asyncNextSpanIdAccessor.get(target);
        recorder.recordNextSpanId(nextSpanId);
        
        String remoteAddress = this.asyncCallRemoteAddressAccessor.get(target);
        recorder.recordDestinationId(remoteAddress);
        
        String methodUri = ThriftUtils.getAsyncMethodCallName((TAsyncMethodCall<?>)target);
        String thriftUrl = remoteAddress + "/" + methodUri;
        recorder.recordAttribute(THRIFT_URL, thriftUrl);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        super.after(target, args, result, throwable);

        // End async trace block if TAsyncMethodCall.cleanUpAndFireCallback(...) call completed successfully
        // if there was an exception, TAsyncMethodCall.onError(...) will be called and the async trace block will be ended there
        if (throwable != null) {
            return;
        }
        Boolean endAsyncBlock = this.asyncCallEndFlagAccessor.get(target);
        if (endAsyncBlock != null && endAsyncBlock) {
            final Trace trace = super.traceContext.currentTraceObject();
            // shouldn't be null
            if (trace == null) {
                return;
            }
            
            if(trace.isAsync() && trace.isRootStack()) {
                trace.close();
                super.traceContext.removeTraceObject();
            }
        }
    }

    @Override
    protected boolean validate(Object target) {
        if (!(target instanceof TAsyncMethodCall)) {
            return false;
        }
        if (!this.asyncNextSpanIdAccessor.isApplicable(target)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need metadata accessor({})", METADATA_ASYNC_NEXT_SPAN_ID);
            }
            return false;
        }
        if (!(this.asyncNextSpanIdAccessor.get(target) instanceof Long)) {
            if (isDebug) {
                logger.debug("Invalid value for metadata {}", METADATA_ASYNC_NEXT_SPAN_ID);
            }
            return false;
        }
        if (!this.asyncCallRemoteAddressAccessor.isApplicable(target)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need metadata accessor({})", METADATA_ASYNC_CALL_REMOTE_ADDRESS);
            }
            return false;
        }
        if (!(this.asyncCallRemoteAddressAccessor.get(target) instanceof String)) {
            if (isDebug) {
                logger.debug("Invalid value for metadata {}", METADATA_ASYNC_CALL_REMOTE_ADDRESS);
            }
            return false;
        }
        if (!this.asyncCallEndFlagAccessor.isApplicable(target)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need metadata accessor({})", METADATA_ASYNC_CALL_END_FLAG);
            }
            return false;
        }
        return super.validate(target);
    }
    
    @Override
    protected ServiceType getServiceType() {
        return THRIFT_CLIENT;
    }
}