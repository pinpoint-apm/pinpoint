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
public final class DepthScope implements Scope {

    public static final int ZERO = 0;

    private final NamedThreadLocal<Depth> scope;


    public DepthScope(final String scopeName) {
        this.scope = new NamedThreadLocal<Depth>(scopeName) {
            @Override
            protected Depth initialValue() {
                return new Depth();
            }
        };
    }

    @Override
    public int push() {
        final Depth depth = scope.get();
        return depth.push();
    }

    @Override
    public int depth() {
        final Depth depth = scope.get();
        return depth.depth();
    }

    @Override
    public int pop() {
        final Depth depth = scope.get();
        return depth.pop();
    }

    private static final class Depth {
        private int depth = 0;

        public int push() {
            return depth++;
        }

        public int pop() {
            return --depth;
        }

        public int depth() {
            return depth;
        }

    }

    @Override
    public String getName() {
        return scope.getName();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("DepthScope{");
        sb.append("scope=").append(scope.getName());
        sb.append('}');
        return sb.toString();
    }
}