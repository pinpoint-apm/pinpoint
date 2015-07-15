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
public class TAsyncMethodCallOnErrorInterceptor extends TAsyncMethodCallInternalMethodInterceptor {
    
    public TAsyncMethodCallOnErrorInterceptor(
            TraceContext traceContext,
            MethodDescriptor methodDescriptor,
            @Name(METADATA_ASYNC_MARKER) MetadataAccessor asyncMarkerAccessor) {
        super(traceContext, methodDescriptor, asyncMarkerAccessor);
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        super.after(target, args, result, throwable);
        
        // End async trace block
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
