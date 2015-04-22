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

import com.navercorp.pinpoint.bootstrap.instrument.InterceptorGroupDefinition;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupInvocation;

/**
 * @author emeroad
 */
public class ThreadLocalScopePool implements ScopePool {

    private final ConcurrentMap<InterceptorGroupDefinition, InterceptorGroupInvocation> pool = new ConcurrentHashMap<InterceptorGroupDefinition, InterceptorGroupInvocation>();

    @Override
    public InterceptorGroupInvocation getScope(InterceptorGroupDefinition scopeDefinition) {
        if (scopeDefinition == null) {
            throw new NullPointerException("scopeDefinition must not be null");
        }
        final InterceptorGroupInvocation scope = this.pool.get(scopeDefinition);
        if (scope != null) {
            return scope;
        }

        final InterceptorGroupInvocation newScope = createScope(scopeDefinition);
        final InterceptorGroupInvocation exist = this.pool.putIfAbsent(scopeDefinition, newScope);
        if (exist != null) {
            return exist;
        }
        return newScope;
    }

    private InterceptorGroupInvocation createScope(InterceptorGroupDefinition scopeDefinition) {
        return new ThreadLocalScope(scopeDefinition);
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ScopePool{");
        sb.append("pool=").append(pool);
        sb.append('}');
        return sb.toString();
    }
}
