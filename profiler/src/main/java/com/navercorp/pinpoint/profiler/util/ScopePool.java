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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.navercorp.pinpoint.bootstrap.instrument.Scope;

/**
 * @author emeroad
 */
public class ScopePool {

    private final ConcurrentMap<String, Scope> pool = new ConcurrentHashMap<String, Scope>();

    public Scope getScope(String scopeName) {
        if (scopeName == null) {
            throw new NullPointerException("scopeName must not be null");
        }
        final Scope scope = this.pool.get(scopeName);
        if (scope != null) {
            return scope;
        }
        final ScopeFactory factory = new SimpleScopeFactory(scopeName);
        final Scope newScope = new ThreadLocalScope(factory);
        final Scope exist = this.pool.putIfAbsent(scopeName, newScope);
        if (exist != null) {
            return exist;
        }
        return newScope;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ScopePool{");
        sb.append("pool=").append(pool);
        sb.append('}');
        return sb.toString();
    }
}
