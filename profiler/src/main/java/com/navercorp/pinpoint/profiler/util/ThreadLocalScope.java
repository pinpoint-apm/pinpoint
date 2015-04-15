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

import com.navercorp.pinpoint.bootstrap.instrument.AttachmentFactory;
import com.navercorp.pinpoint.bootstrap.instrument.InterceptorGroupDefinition;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupTransaction;
import com.navercorp.pinpoint.profiler.plugin.DefaultInterceptorStack;

/**
 * @author emeroad
 */
public class ThreadLocalScope implements InterceptorGroupTransaction {

    private final NamedThreadLocal<InterceptorGroupTransaction> scope;


    public ThreadLocalScope(final InterceptorGroupDefinition scopeDefinition) {
        if (scopeDefinition == null) {
            throw new NullPointerException("scopeDefinition must not be null");
        }
        
        this.scope = new NamedThreadLocal<InterceptorGroupTransaction>(scopeDefinition.getName()) {
            @Override
            protected InterceptorGroupTransaction initialValue() {
                return new DefaultInterceptorStack(scopeDefinition.getName());
            }
        };
    }
    
    @Override
    public void leave(ExecutionPolicy policy) {
        final InterceptorGroupTransaction localScope = getLocalScope();
        localScope.leave(policy);
    }

    @Override
    public boolean tryEnter(ExecutionPolicy policy) {
        final InterceptorGroupTransaction localScope = getLocalScope();
        return localScope.tryEnter(policy);
    }

    @Override
    public boolean canLeave(ExecutionPolicy policy) {
        final InterceptorGroupTransaction localScope = getLocalScope();
        return localScope.canLeave(policy);
    }

    protected InterceptorGroupTransaction getLocalScope() {
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
        final InterceptorGroupTransaction localScope = getLocalScope();
        return localScope.isActive();
    }

    @Override
    public Object setAttachment(Object attachment) {
        final InterceptorGroupTransaction localScope = getLocalScope();
        return localScope.setAttachment(attachment);
    }

    @Override
    public Object getAttachment() {
        final InterceptorGroupTransaction localScope = getLocalScope();
        return localScope.getAttachment();
    }
    
    @Override
    public Object getOrCreateAttachment(AttachmentFactory factory) {
        final InterceptorGroupTransaction localScope = getLocalScope();
        return localScope.getOrCreateAttachment(factory);
    }

    @Override
    public Object removeAttachment() {
        final InterceptorGroupTransaction localScope = getLocalScope();
        return localScope.removeAttachment();
    }
}