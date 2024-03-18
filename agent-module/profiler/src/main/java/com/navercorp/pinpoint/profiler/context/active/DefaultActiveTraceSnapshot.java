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

import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class DefaultActiveTraceSnapshot implements ActiveTraceSnapshot {

    private final LocalTraceRoot traceRoot;

    public static ActiveTraceSnapshot of(LocalTraceRoot localTraceRoot) {
        return new DefaultActiveTraceSnapshot(localTraceRoot);
    }

    DefaultActiveTraceSnapshot(LocalTraceRoot traceRoot) {
        this.traceRoot = Objects.requireNonNull(traceRoot, "traceRoot");
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
        if (isRemote()) {
            return true;
        }
        return false;
    }

    @Override
    public String getTransactionId() {
        if (isRemote()) {
            return ((TraceRoot) traceRoot).getTraceId().getTransactionId();
        }
        return null;
    }

    @Override
    public String getEntryPoint() {
        if (isRemote()) {
            return traceRoot.getShared().getRpcName();
        }
        return null;
    }

    private boolean isRemote() {
        return traceRoot instanceof TraceRoot;
    }

    @Override
    public String toString() {
        return "SampledActiveTraceSnapshot{" +
                "traceRoot=" + traceRoot +
                '}';
    }
}
