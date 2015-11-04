/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.Trace;

/**
 * @author HyunGil Jeong
 */
public class ActiveTrace {
    
    private final Trace trace;
    
    public ActiveTrace(Trace trace) {
        if (trace == null) {
            throw new NullPointerException("trace must not be null");
        }
        this.trace = trace;
    }

    public long getId() {
        return this.trace.getId();
    }

    public long getStartTime() {
        return this.trace.getStartTime();
    }

    public Thread getBindThread() {
        return this.trace.getBindThread();
    }

}
