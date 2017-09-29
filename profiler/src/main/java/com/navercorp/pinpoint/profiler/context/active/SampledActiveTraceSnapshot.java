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

package com.navercorp.pinpoint.profiler.context.active;

import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;

/**
 * @author Taejin Koo
 */
public class SampledActiveTraceSnapshot implements ActiveTraceSnapshot {

    private final TraceRoot traceRoot;

    public SampledActiveTraceSnapshot(TraceRoot traceRoot) {
        this.traceRoot = Assert.requireNonNull(traceRoot, "traceRoot must not be null");

    }

    @Override
    public long getLocalTransactionId() {
        return traceRoot.getLocalTransactionId();
    }

    @Override
    public long getStartTime() {
        return traceRoot.getTraceStartTime();
    }

    @Override
    public long getThreadId() {
        return traceRoot.getShared().getThreadId();
    }

    @Override
    public boolean isSampled() {
        return true;
    }

    @Override
    public String getTransactionId() {
        return traceRoot.getTraceId().getTransactionId();
    }

    @Override
    public String getEntryPoint() {
        return traceRoot.getShared().getRpcName();
    }

    @Override
    public String toString() {
        return "SampledActiveTraceSnapshot{" +
                "traceRoot=" + traceRoot +
                '}';
    }
}
