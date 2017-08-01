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


import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;

/**
 * @author HyunGil Jeong
 */
public class SampledActiveTrace implements ActiveTrace {

    private final TraceRoot traceRoot;

    public SampledActiveTrace(TraceRoot traceRoot) {
        this.traceRoot = Assert.requireNonNull(traceRoot, "trace must not be null");
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
        return new SampledActiveTraceSnapshot(traceRoot);
    }

    @Override
    public String toString() {
        return "SampledActiveTrace{" +
                "traceRoot=" + traceRoot +
                '}';
    }
}
