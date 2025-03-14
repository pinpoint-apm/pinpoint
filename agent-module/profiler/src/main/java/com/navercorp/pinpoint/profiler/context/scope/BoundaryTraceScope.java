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

import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class BoundaryTraceScope implements TraceScope {
    private final String name;
    private int depth = 0;
    private int skippedBoundary = 0;

    public BoundaryTraceScope(String name) {
        this.name = Objects.requireNonNull(name, "name");
    }

    @Override
    public String getName() {
        return name;
    }

    public boolean tryEnter() {
        if (isActive()) {
            skippedBoundary++;
            return false;
        } else {
            depth++;
            return true;
        }
    }

    public boolean canLeave() {
        if (skippedBoundary == 0 && depth == 1) {
            return true;
        } else {
            skippedBoundary--;
            return false;
        }
    }

    public void leave() {
        if (!isActive()) {
            throw new IllegalStateException("cannot leave with trace scope. depth: " + depth);
        }

        if (skippedBoundary != 0 || depth != 1) {
            throw new IllegalStateException("cannot leave with BOUNDARY trace scope. depth: " + depth);
        }
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
        sb.append(", skippedBoundary=").append(skippedBoundary);
        sb.append('}');
        return sb.toString();
    }
}