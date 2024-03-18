/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.context.active;


import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
public class DefaultActiveTrace implements ActiveTrace {

    private final LocalTraceRoot traceRoot;

    public DefaultActiveTrace(LocalTraceRoot traceRoot) {
        this.traceRoot = Objects.requireNonNull(traceRoot, "trace");
    }

    @Override
    public long getStartTime() {
        return this.traceRoot.getTraceStartTime();
    }

    @Override
    public long getId() {
        return this.traceRoot.getLocalTransactionId();
    }


    @Override
    public ActiveTraceSnapshot snapshot() {
        return DefaultActiveTraceSnapshot.of(traceRoot);
    }

    @Override
    public String toString() {
        return "SampledActiveTrace{" +
                "traceRoot=" + traceRoot +
                '}';
    }
}
