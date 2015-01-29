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
public class SimpleScope implements Scope {

    private final String name;

    private int depth = 0;

    public SimpleScope(String name) {
        this.name = name;
    }

    public int push() {
        return depth++;
    }

    public int pop() {
        return --depth;
    }

    public int depth() {
        return depth;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SimpleScope{");
        sb.append("name=").append(name);
        sb.append('}');
        return sb.toString();
    }
}
