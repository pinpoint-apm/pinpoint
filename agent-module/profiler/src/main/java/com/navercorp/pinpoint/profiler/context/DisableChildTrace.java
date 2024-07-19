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

import com.navercorp.pinpoint.bootstrap.context.RequestId;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;
import com.navercorp.pinpoint.profiler.context.scope.DefaultTraceScopePool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

public class DisableChildTrace implements Trace {
    // private static final int ASYNC_BEGIN_STACK_ID = 1001;
    public static final String UNSUPPORTED_OPERATION = "disable async child trace";

    protected final Logger logger = LogManager.getLogger(getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    private boolean closed = false;

    private DefaultTraceScopePool scopePool;

    private final LocalTraceRoot traceRoot;
    private int depth;

    private final SpanRecorder spanRecorder;
    private final SpanEventRecorder spanEventRecorder;

    private RequestId requestId;

    public DisableChildTrace(final LocalTraceRoot traceRoot, SpanRecorder spanRecorder, SpanEventRecorder spanEventRecorder) {
        this.traceRoot = Objects.requireNonNull(traceRoot, "traceRoot");
        this.spanRecorder = Objects.requireNonNull(spanRecorder, "spanRecorder");
        this.spanEventRecorder = Objects.requireNonNull(spanEventRecorder, "spanEventRecorder");
    }

    @Override
    public SpanEventRecorder traceBlockBegin() {
        return traceBlockBegin(DEFAULT_STACKID);
    }

    @Override
    public SpanEventRecorder traceBlockBegin(int stackId) {
        push();
        return getSpanEventRecorder();
    }

    @Override
    public void traceBlockEnd() {
        traceBlockBegin(DEFAULT_STACKID);
    }


    @Override
    public void traceBlockEnd(int stackId) {
        pop();
    }

    private SpanEventRecorder getSpanEventRecorder() {
        return spanEventRecorder;
    }

    private int push() {
        return this.depth++;
    }

    private void pop() {
        this.depth--;
    }

    @Override
    public boolean isRootStack() {
        return depth == 0;
    }

    @Override
    public int getCallStackFrameId() {
        return DEFAULT_STACKID;
    }

    private LocalTraceRoot getTraceRoot() {
        return this.traceRoot;
    }

    @Override
    public long getId() {
        return getTraceRoot().getLocalTransactionId();
    }

    @Override
    public long getStartTime() {
        return traceRoot.getTraceStartTime();
    }

    @Override
    public TraceId getTraceId() {
        throw new UnsupportedOperationException(UNSUPPORTED_OPERATION);
    }

    @Override
    public RequestId getRequestId() {
        return requestId;
    }

    @Override
    public void setRequestId(RequestId id) {
        this.requestId = id;
    }

    @Override
    public boolean canSampled() {
        return false;
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public SpanRecorder getSpanRecorder() {
        return spanRecorder;
    }

    @Override
    public SpanEventRecorder currentSpanEventRecorder() {
        return spanEventRecorder;
    }

    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        if (closed) {
            logger.debug("Already closed");
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
        return "DisableChildTrace{" +
                "traceRoot=" + traceRoot +
                '}';
    }
}
