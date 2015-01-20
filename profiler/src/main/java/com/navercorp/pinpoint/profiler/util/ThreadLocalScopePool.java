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
import com.navercorp.pinpoint.bootstrap.instrument.ScopeDefinition;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author emeroad
 */
public class ThreadLocalScopePool implements ScopePool {

    private final ConcurrentMap<ScopeDefinition, Scope> pool = new ConcurrentHashMap<ScopeDefinition, Scope>();

    @Override
    public Scope getScope(ScopeDefinition scopeDefinition) {
        if (scopeDefinition == null) {
            throw new NullPointerException("scopeDefinition must not be null");
        }
        final Scope scope = this.pool.get(scopeDefinition);
        if (scope != null) {
            return scope;
        }

        final Scope newScope = createScope(scopeDefinition);
        final Scope exist = this.pool.putIfAbsent(scopeDefinition, newScope);
        if (exist != null) {
            return exist;
        }
        return newScope;
    }

    private Scope createScope(ScopeDefinition scopeDefinition) {

        if (scopeDefinition.getType() == ScopeDefinition.Type.ATTACHMENT) {

            AttachmentSimpleScopeFactory<Object> factory = new AttachmentSimpleScopeFactory<Object>(scopeDefinition.getName());
            return new AttachmentThreadLocalScope<Object>(factory);

        } else if (scopeDefinition.getType() == ScopeDefinition.Type.SIMPLE) {

            SimpleScopeFactory simpleScopeFactory = new SimpleScopeFactory(scopeDefinition.getName());
            return new ThreadLocalScope(simpleScopeFactory);

        } else {
            throw new UnsupportedOperationException(scopeDefinition.getType() + "type support");
        }

    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ScopePool{");
        sb.append("pool=").append(pool);
        sb.append('}');
        return sb.toString();
    }
}
