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
import com.navercorp.pinpoint.bootstrap.instrument.Scope;
import com.navercorp.pinpoint.bootstrap.instrument.ScopeDefinition;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPoint;
import com.navercorp.pinpoint.profiler.plugin.DefaultScope;

/**
 * @author emeroad
 */
public class ThreadLocalScope implements Scope {

    private final NamedThreadLocal<Scope> scope;


    public ThreadLocalScope(final ScopeDefinition scopeDefinition) {
        if (scopeDefinition == null) {
            throw new NullPointerException("scopeDefinition must not be null");
        }
        
        this.scope = new NamedThreadLocal<Scope>(scopeDefinition.getName()) {
            @Override
            protected Scope initialValue() {
                return new DefaultScope(scopeDefinition.getName());
            }
        };
    }

    @Override
    public boolean tryBefore(ExecutionPoint point) {
        final Scope localScope = getLocalScope();
        return localScope.tryBefore(point);
    }

    @Override
    public boolean tryAfter(ExecutionPoint point) {
        final Scope localScope = getLocalScope();
        return localScope.tryAfter(point);
    }

    protected Scope getLocalScope() {
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
    public boolean isIn() {
        final Scope localScope = getLocalScope();
        return localScope.isIn();
    }

    @Override
    public Object setAttachment(Object attachment) {
        final Scope localScope = getLocalScope();
        return localScope.setAttachment(attachment);
    }

    @Override
    public Object getAttachment() {
        final Scope localScope = getLocalScope();
        return localScope.getAttachment();
    }
    
    @Override
    public Object getOrCreateAttachment(AttachmentFactory factory) {
        final Scope localScope = getLocalScope();
        return localScope.getOrCreateAttachment(factory);
    }

    @Override
    public Object removeAttachment() {
        final Scope localScope = getLocalScope();
        return localScope.removeAttachment();
    }
}