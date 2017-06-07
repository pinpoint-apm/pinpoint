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

import com.navercorp.pinpoint.bootstrap.context.AsyncState;
import com.navercorp.pinpoint.bootstrap.context.AsyncStateSupport;
import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.common.annotations.InterfaceAudience;

/**
 * @author jaehong.kim
 */
@InterfaceAudience.LimitedPrivate("vert.x")
public class StatefulAsyncTraceId implements AsyncTraceId, AsyncStateSupport, TraceRootSupport {
    private final TraceRoot traceRoot;
    private final AsyncTraceId asyncTraceId;
    private final AsyncState asyncState;

    public StatefulAsyncTraceId(final TraceRoot traceRoot, final AsyncTraceId asyncTraceId, final AsyncState asyncState) {
        if (traceRoot == null) {
            throw new NullPointerException("traceRoot must not be null");
        }
        if (asyncTraceId == null ) {
            throw new IllegalArgumentException("asyncTraceId must not be null");
        }
        if (asyncState == null) {
            throw new NullPointerException("asyncState must not be null");
        }
        this.traceRoot = traceRoot;
        this.asyncTraceId = asyncTraceId;
        this.asyncState = asyncState;
    }

    @Override
    public int getAsyncId() {
        return asyncTraceId.getAsyncId();
    }

    @Override
    public short nextAsyncSequence() {
        return asyncTraceId.nextAsyncSequence();
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
        return this.traceRoot.getTraceStartTime();
    }

    @Override
    public TraceId getParentTraceId() {
        return getTraceId0();
    }

    @Override
    public AsyncState getAsyncState() {
        return asyncState;
    }

    @Override
    public TraceRoot getTraceRoot() {
        return traceRoot;
    }

    private TraceId getTraceId0() {
        return this.traceRoot.getTraceId();
    }

}
