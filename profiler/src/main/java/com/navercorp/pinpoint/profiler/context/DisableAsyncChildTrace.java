/*
 * Copyright 2022 NAVER Corp.
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

import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.scope.DefaultTraceScopePool;

import java.util.Objects;

public class DisableAsyncChildTrace implements Trace {
    // private static final int ASYNC_BEGIN_STACK_ID = 1001;
    public static final String UNSUPPORTED_OPERATION = "disable async child trace";

    private boolean closed = false;

    private DefaultTraceScopePool scopePool;

    private final TraceRoot traceRoot;
    private final LocalAsyncId localAsyncId;

    public DisableAsyncChildTrace(final TraceRoot traceRoot, final LocalAsyncId localAsyncId) {
        this.traceRoot = Objects.requireNonNull(traceRoot, "traceRoot");
        this.localAsyncId = Objects.requireNonNull(localAsyncId, "localAsyncId");
    }

    @Override
    public SpanEventRecorder traceBlockBegin() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public SpanEventRecorder traceBlockBegin(int stackId) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public void traceBlockEnd() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public void traceBlockEnd(int stackId) {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public boolean isRootStack() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public int getCallStackFrameId() {
        return 0;
    }

    private TraceRoot getTraceRoot() {
        return this.traceRoot;
    }

    @Override
    public long getId() {
        return getTraceRoot().getLocalTransactionId();
    }

    @Override
    public long getStartTime() {
        return getTraceRoot().getTraceStartTime();
    }

    @Override
    public TraceId getTraceId() {
        return getTraceRoot().getTraceId();
    }

    @Override
    public boolean canSampled() {
        return false;
    }

    @Override
    public boolean isRoot() {
        return this.getTraceId().isRoot();
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public SpanRecorder getSpanRecorder() {
        return null;
    }

    @Override
    public SpanEventRecorder currentSpanEventRecorder() {
        return null;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
    }

    @Override
    public TraceScope getScope(String name) {
        if (scopePool == null) {
            return null;
        }
        return scopePool.get(name);
    }

    @Override
    public TraceScope addScope(String name) {
        if (scopePool == null) {
            scopePool = new DefaultTraceScopePool();
        }
        return scopePool.add(name);
    }

    @Override
    public String toString() {
        return "DisableAsyncChildTrace{" +
            "traceRoot=" + getTraceRoot() +
            ", localAsyncId=" + localAsyncId +
            '}';
    }
}
