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

package com.navercorp.pinpoint.profiler.interceptor;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.ByteCodeMethodDescriptorSupport;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupInvocation;

/**
 * @author emeroad
 */
public class GroupDelegateSimpleInterceptor implements SimpleAroundInterceptor, ByteCodeMethodDescriptorSupport, TraceContextSupport {

    private final SimpleAroundInterceptor delegate;
    private final InterceptorGroupInvocation scope;


    public GroupDelegateSimpleInterceptor(SimpleAroundInterceptor delegate, InterceptorGroupInvocation scope) {
        if (delegate == null) {
            throw new NullPointerException("delegate must not be null");
        }
        if (scope == null) {
            throw new NullPointerException("scope must not be null");
        }
        this.delegate = delegate;
        this.scope = scope;
    }

    @Override
    public void before(Object target, Object[] args) {
        if (scope.tryEnter(ExecutionPolicy.BOUNDARY)) {
            this.delegate.before(target, args);
        }
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        if (scope.canLeave(ExecutionPolicy.BOUNDARY)) {
            this.delegate.after(target, args, result, throwable);
            scope.leave(ExecutionPolicy.BOUNDARY);
        }
    }

    @Override
    public void setMethodDescriptor(MethodDescriptor descriptor) {
        if (this.delegate instanceof ByteCodeMethodDescriptorSupport) {
            ((ByteCodeMethodDescriptorSupport) this.delegate).setMethodDescriptor(descriptor);
        }
    }

    @Override
    public void setTraceContext(TraceContext traceContext) {
        if (this.delegate instanceof TraceContextSupport) {
            ((TraceContextSupport) this.delegate).setTraceContext(traceContext);
        }
    }
}
