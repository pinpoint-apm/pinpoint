/*
 * Copyright 2015 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.context.scope;

import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author jaehong.kim
 */
public class DefaultTraceScope implements TraceScope {
    private final String name;
    private int depth = 0;

    public DefaultTraceScope(String name) {
        this.name = Assert.requireNonNull(name, "name");
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean tryEnter() {
        // policy is ALWAYS
        depth++;
        return true;
    }

    public boolean canLeave() {
        if (!isActive()) {
            return false;
        }

        return true;
    }

    public void leave() {
        if (!isActive()) {
            throw new IllegalStateException("Cannot leave with scope. depth: " + depth);
        }

        // policy is ALWAYS
        depth--;
    }

    @Override
    public boolean isActive() {
        return depth > 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DefaultTraceScope{");
        sb.append("name='").append(name).append('\'');
        sb.append(", depth=").append(depth);
        sb.append('}');
        return sb.toString();
    }
}