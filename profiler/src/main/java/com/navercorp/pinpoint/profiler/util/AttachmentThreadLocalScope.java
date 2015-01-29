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
import com.navercorp.pinpoint.bootstrap.instrument.AttachmentScope;
import com.navercorp.pinpoint.bootstrap.instrument.Scope;

/**
 * @author emeroad
 */
public class AttachmentThreadLocalScope<T> implements AttachmentScope<T> {

    private final NamedThreadLocal<AttachmentScope<T>> scope;

    public AttachmentThreadLocalScope(final AttachmentSimpleScopeFactory<T> attachmentSimpleScopeFactory) {
        if (attachmentSimpleScopeFactory == null) {
            throw new NullPointerException("attachmentSimpleScopeFactory must not be null");
        }

        this.scope = new NamedThreadLocal<AttachmentScope<T>>(attachmentSimpleScopeFactory.getName()) {

            @Override
            @SuppressWarnings("unchecked")
            protected AttachmentScope<T> initialValue() {
                final Scope newScope = attachmentSimpleScopeFactory.createScope();
                if (newScope instanceof AttachmentScope) {
                    return (AttachmentScope<T>) newScope;
                }
                throw new IllegalArgumentException("invalid scope type");
            }
        };

    }

    @Override
    public int push() {
        final AttachmentScope<T> localScope = getLocalScope();
        return localScope.push();
    }

    @Override
    public int depth() {
        final AttachmentScope<T> localScope = getLocalScope();
        return localScope.depth();
    }

    @Override
    public int pop() {
        final AttachmentScope<T> localScope = getLocalScope();
        return localScope.pop();
    }

    protected AttachmentScope<T> getLocalScope() {
        return scope.get();
    }


    @Override
    public String getName() {
        return scope.getName();
    }

    @Override
    public T getOrCreate(AttachmentFactory<T> attachmentFactory) {
        final AttachmentScope<T> localScope = getLocalScope();
        return localScope.getOrCreate(attachmentFactory);
    }

    @Override
    public void setAttachment(T object) {
        final AttachmentScope<T> localScope = getLocalScope();
        localScope.setAttachment(object);
    }

    @Override
    public T getAttachment() {
        final AttachmentScope<T> localScope = getLocalScope();
        return localScope.getAttachment();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("AttachmentThreadLocalScope{");
        sb.append("scope=").append(scope.getName());
        sb.append('}');
        return sb.toString();
    }
}