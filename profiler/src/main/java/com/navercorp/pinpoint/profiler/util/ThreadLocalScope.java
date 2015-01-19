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

import com.navercorp.pinpoint.bootstrap.instrument.Scope;

/**
 * @author emeroad
 */
public final class ThreadLocalScope implements Scope {

    private final NamedThreadLocal<Scope> scope;


    public ThreadLocalScope(final ScopeFactory scopeFactory) {
        if (scopeFactory == null) {
            throw new NullPointerException("scopeFactory must not be null");
        }
        this.scope = new NamedThreadLocal<Scope>(scopeFactory.getName()) {
            @Override
            protected Scope initialValue() {
                return scopeFactory.createScope();
            }
        };
    }

    @Override
    public int push() {
        final Scope depth = getScope();
        return depth.push();
    }

    @Override
    public int depth() {
        final Scope depth = getScope();
        return depth.depth();
    }

    @Override
    public int pop() {
        final Scope depth = getScope();
        return depth.pop();
    }

    private Scope getScope() {
        return scope.get();
    }


    @Override
    public String getName() {
        return toString();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ThreadLocalScope{");
        sb.append("scope=").append(scope.getName());
        sb.append('}');
        return sb.toString();
    }
}