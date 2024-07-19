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

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.RequestId;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.profiler.context.id.LocalTraceRoot;
import com.navercorp.pinpoint.profiler.context.id.Shared;
import com.navercorp.pinpoint.profiler.context.scope.DefaultTraceScopePool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;


/**
 * @author emeroad
 * @author jaehong.kim
 */
public class DisableTrace implements Trace {
    protected final Logger logger = LogManager.getLogger(getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    public static final String UNSUPPORTED_OPERATION = "disable trace";
    public static final long DISABLE_TRACE_OBJECT_ID = -1;

    private final LocalTraceRoot traceRoot;
    private final SpanRecorder spanRecorder;
    private DefaultTraceScopePool scopePool;
    private final CloseListener closeListener;

    private int depth;

    private SpanEventRecorder spanEventRecorder;

    private boolean closed = false;

    private RequestId requestId;

    public DisableTrace(LocalTraceRoot traceRoot,
                        SpanRecorder spanRecorder, SpanEventRecorder spanEventRecorder,
                        CloseListener closeListener) {
        this.traceRoot = Objects.requireNonNull(traceRoot, "traceRoot");
        this.spanRecorder = Objects.requireNonNull(spanRecorder, "spanRecorder");
        this.spanEventRecorder = Objects.requireNonNull(spanEventRecorder, "spanEventRecorder");

        this.closeListener = Objects.requireNonNull(closeListener, "closeListener");

        setCurrentThread();
    }

    private void setCurrentThread() {
        final long threadId = Thread.currentThread().getId();
        getShared().setThreadId(threadId);
    }

    @Override
    public long getId() {
        return traceRoot.getLocalTransactionId();
    }

    @Override
    public long getStartTime() {
        return traceRoot.getTraceStartTime();
    }


    @Override
    public SpanEventRecorder traceBlockBegin() {
        return traceBlockBegin(DEFAULT_STACKID);
    }

    @Override
    public SpanEventRecorder traceBlockBegin(int stackId) {
        push();
        return this.spanEventRecorder;
    }

    @Override
    public void traceBlockEnd() {
        traceBlockBegin(DEFAULT_STACKID);
    }

    @Override
    public void traceBlockEnd(int stackId) {
        pop();
    }

    private int push() {
        return this.depth++;
    }

    private void pop() {
        this.depth--;
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
        // always return false
        return false;
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isRootStack() {
        return depth == 0;
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

        final long purgeTime = System.currentTimeMillis();
        this.closeListener.close(purgeTime);
    }

    protected void flush() {
        this.closed = true;
    }

    private Shared getShared() {
        return traceRoot.getShared();
    }

    @Override
    public int getCallStackFrameId() {
        return DEFAULT_STACKID;
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

}