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
package com.navercorp.pinpoint.profiler.context.id;


import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.TraceId;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author jaehong.kim
 */
public class DefaultAsyncTraceId implements AsyncTraceId, TraceRootSupport {

    private static final AtomicIntegerFieldUpdater<DefaultAsyncTraceId> ASYNC_SEQUENCE_UPDATER
            = AtomicIntegerFieldUpdater.newUpdater(DefaultAsyncTraceId.class, "asyncSequence");

    private final TraceRoot traceRoot;
    private final int asyncId;

    @SuppressWarnings("unused")
    private volatile int asyncSequence = 0;

    public DefaultAsyncTraceId(final TraceRoot traceRoot, final int asyncId) {
        if (traceRoot == null) {
            throw new IllegalArgumentException("traceRoot must not be null.");
        }
        this.traceRoot = traceRoot;
        this.asyncId = asyncId;
    }

    private TraceId getTraceId0() {
        return traceRoot.getTraceId();
    }

    public int getAsyncId() {
        return asyncId;
    }

    public short nextAsyncSequence() {
        return (short) ASYNC_SEQUENCE_UPDATER.incrementAndGet(this);
    }

    @Override
    public TraceId getNextTraceId() {
        return getTraceId0().getNextTraceId();
    }


    @Override
    public long getSpanId() {
        return getTraceId0().getSpanId();
    }

    @Override
    public String getTransactionId() {
        return getTraceId0().getTransactionId();
    }

    @Override
    public String getAgentId() {
        return getTraceId0().getAgentId();
    }

    @Override
    public long getAgentStartTime() {
        return getTraceId0().getAgentStartTime();
    }

    @Override
    public long getTransactionSequence() {
        return getTraceId0().getTransactionSequence();
    }

    @Override
    public long getParentSpanId() {
        return getTraceId0().getParentSpanId();
    }

    @Override
    public short getFlags() {
        return getTraceId0().getFlags();
    }

    @Override
    public boolean isRoot() {
        return getTraceId0().isRoot();
    }

    @Override
    public long getSpanStartTime() {
        return traceRoot.getTraceStartTime();
    }

    @Override
    public TraceRoot getTraceRoot() {
        return traceRoot;
    }

    @Override
    public TraceId getParentTraceId() {
        return getTraceId0();
    }

    @Override
    public String toString() {
        return "DefaultAsyncTraceId{" +
                "traceRoot=" + traceRoot +
                ", asyncId=" + asyncId +
                ", asyncSequence=" + asyncSequence +
                '}';
    }
}