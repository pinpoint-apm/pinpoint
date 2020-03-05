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

package com.navercorp.pinpoint.profiler.util;

import com.navercorp.pinpoint.bootstrap.instrument.InterceptorScopeDefinition;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.AttachmentFactory;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScopeInvocation;
import com.navercorp.pinpoint.profiler.interceptor.scope.DefaultInterceptorScopeInvocation;

/**
 * @author emeroad
 */
public class ThreadLocalScope implements InterceptorScopeInvocation {

    private final NamedThreadLocal<InterceptorScopeInvocation> scope;


    public ThreadLocalScope(final InterceptorScopeDefinition scopeDefinition) {
        if (scopeDefinition == null) {
            throw new NullPointerException("scopeDefinition");
        }
        
        this.scope = new NamedThreadLocal<InterceptorScopeInvocation>(scopeDefinition.getName()) {
            @Override
            protected InterceptorScopeInvocation initialValue() {
                return new DefaultInterceptorScopeInvocation(scopeDefinition.getName());
            }
        };
    }
    
    @Override
    public void leave(ExecutionPolicy policy) {
        final InterceptorScopeInvocation localScope = getLocalScope();
        localScope.leave(policy);
    }

    @Override
    public boolean tryEnter(ExecutionPolicy policy) {
        final InterceptorScopeInvocation localScope = getLocalScope();
        return localScope.tryEnter(policy);
    }

    @Override
    public boolean canLeave(ExecutionPolicy policy) {
        final InterceptorScopeInvocation localScope = getLocalScope();
        return localScope.canLeave(policy);
    }

    protected InterceptorScopeInvocation getLocalScope() {
        return scope.get();
    }


    @Override
    public String getName() {
        return scope.getName();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ThreadLocalScope{");
        sb.append("scope=").append(scope.getName());
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean isActive() {
        final InterceptorScopeInvocation localScope = getLocalScope();
        return localScope.isActive();
    }

    @Override
    public Object setAttachment(Object attachment) {
        final InterceptorScopeInvocation localScope = getLocalScope();
        return localScope.setAttachment(attachment);
    }

    @Override
    public Object getAttachment() {
        final InterceptorScopeInvocation localScope = getLocalScope();
        return localScope.getAttachment();
    }
    
    @Override
    public Object getOrCreateAttachment(AttachmentFactory factory) {
        final InterceptorScopeInvocation localScope = getLocalScope();
        return localScope.getOrCreateAttachment(factory);
    }

    @Override
    public Object removeAttachment() {
        final InterceptorScopeInvocation localScope = getLocalScope();
        return localScope.removeAttachment();
    }
}