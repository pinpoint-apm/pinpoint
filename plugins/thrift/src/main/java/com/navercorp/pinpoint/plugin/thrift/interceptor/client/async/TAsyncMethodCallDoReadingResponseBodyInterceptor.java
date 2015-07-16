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
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Name;

/**
 * @author HyunGil Jeong
 */
public class TAsyncMethodCallDoReadingResponseBodyInterceptor extends TAsyncMethodCallInternalMethodInterceptor {
    
    private final MetadataAccessor asyncCallEndFlagAccessor;

    public TAsyncMethodCallDoReadingResponseBodyInterceptor(
            TraceContext traceContext,
            MethodDescriptor methodDescriptor,
            @Name(METADATA_ASYNC_MARKER) MetadataAccessor asyncMarkerAccessor,
            @Name(METADATA_ASYNC_CALL_END_FLAG) MetadataAccessor asyncCallEndFlagAccessor) {
        super(traceContext, methodDescriptor, asyncMarkerAccessor);
        this.asyncCallEndFlagAccessor = asyncCallEndFlagAccessor;
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
        if (!this.asyncCallEndFlagAccessor.isApplicable(target)) {
            if (isDebug) {
                logger.debug("Invalid target object. Need metadata accessor({})", METADATA_ASYNC_CALL_END_FLAG);
            }
            return false;
        }
        return super.validate(target);
    }

}
