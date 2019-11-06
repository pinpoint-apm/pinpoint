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
import com.navercorp.pinpoint.profiler.util.NameValueList;

/**
 * @author jaehong.kim
 */
public class DefaultTraceScopePool {

    private final NameValueList<TraceScope> list = new NameValueList<TraceScope>();

    public TraceScope get(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name");
        }

        return list.get(name);
    }

    public TraceScope add(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name");
        }

        final TraceScope oldScope = list.add(name, new DefaultTraceScope(name));
        return oldScope;
    }

    public void clear() {
        list.clear();
    }
}