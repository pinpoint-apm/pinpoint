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

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncTrace implements Trace {
    private static final int BEGIN_STACKID = 1;

    private static final Logger logger = LoggerFactory.getLogger(AsyncTrace.class.getName());
    private static final boolean isDebug = logger.isDebugEnabled();

    private final AsyncContextFactory asyncContextFactory;

    private final TraceRoot traceRoot;
    private final DefaultTrace trace;
    private final boolean entryPoint;

    private int asyncId;
    private short asyncSequence;
    private AsyncState asyncState;

    public AsyncTrace(final AsyncContextFactory asyncContextFactory, final TraceRoot traceRoot, final DefaultTrace trace, final AsyncState asyncState) {
        this.asyncContextFactory = Assert.requireNonNull(asyncContextFactory, "asyncContextFactory must not be null");
        this.traceRoot = Assert.requireNonNull(traceRoot, "traceRoot must not be null");
        this.trace = Assert.requireNonNull(trace, "trace must not be null");
        this.asyncState = Assert.requireNonNull(asyncState, "asyncState must not be null");
        this.entryPoint = true;
    }

    public AsyncTrace(final AsyncContextFactory asyncContextFactory, final TraceRoot traceRoot, final DefaultTrace trace, final int asyncId, final short asyncSequence) {
        this.asyncContextFactory = Assert.requireNonNull(asyncContextFactory, "asyncContextFactory must not be null");
        this.traceRoot = Assert.requireNonNull(traceRoot, "traceRoot must not be null");
        this.trace = Assert.requireNonNull(trace, "trace must not be null");
        this.asyncId = asyncId;
        this.asyncSequence = asyncSequence;

        this.asyncState = null;
        this.entryPoint = false;

        traceBlockBegin(BEGIN_STACKID);
    }

    public int getAsyncId() {
        return asyncId;
    }

    @Override
    public long getId() {
        if (this.entryPoint) {
            return traceRoot.getLocalTransactionId();
        }

        return -1;
    }

    @Override
    public long getStartTime() {
        if (this.entryPoint) {
            return this.traceRoot.getTraceStartTime();
        }

        return 0;
    }

    @Override
    public Thread getBindThread() {
        return null;
    }

    @Override
    public long getThreadId() {
        if (this.entryPoint) {
            return this.traceRoot.getShared().getThreadId();
        }

        return -1;
    }

    @Override
    public TraceId getTraceId() {
        return this.traceRoot.getTraceId();
    }

    @Override
    public boolean canSampled() {
        return trace.canSampled();
    }

    @Override
    public boolean isRoot() {
        return this.traceRoot.getTraceId().isRoot();
    }

    @Override
    public SpanEventRecorder traceBlockBegin() {
        final SpanEventRecorder recorder = trace.traceBlockBegin();
        if (this.entryPoint) {
            return recorder;
        }
        recorder.recordAsyncId(asyncId);
        recorder.recordAsyncSequence(asyncSequence);
        return recorder;
    }

    @Override
    public SpanEventRecorder traceBlockBegin(int stackId) {
        final SpanEventRecorder recorder = trace.traceBlockBegin(stackId);
        if (this.entryPoint) {
            return recorder;
        }
        recorder.recordAsyncId(asyncId);
        recorder.recordAsyncSequence(asyncSequence);
        return recorder;
    }

    @Override
    public void traceBlockEnd() {
        trace.traceBlockEnd();
    }

    @Override
    public void traceBlockEnd(int stackId) {
        trace.traceBlockEnd(stackId);
    }

    @Override
    public boolean isAsync() {
        if (this.entryPoint) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isRootStack() {
        if (this.entryPoint) {
            return this.trace.isRootStack();
        }
        return trace.getCallStackFrameId() == BEGIN_STACKID;
    }

    @Override
    public AsyncTraceId getAsyncTraceId() {
        return asyncContextFactory.newAsyncTraceId(traceRoot);
    }

    @Override
    public void close() {
        if (this.entryPoint) {
            closeOrFlush();
        } else {
            traceBlockEnd(BEGIN_STACKID);
            trace.close();
        }
    }

    private void closeOrFlush() {
        final AsyncState asyncState = this.asyncState;
        if (asyncState == null) {
            return;
        }

        if (asyncState.await()) {
            // flush.
            this.trace.flush();
            if (isDebug) {
                logger.debug("Flush trace={}, asyncState={}", this, this.asyncState);
            }
        } else {
            // close.
            this.trace.close();
            if (isDebug) {
                logger.debug("Close trace={}. asyncState={}", this, this.asyncState);
            }
        }

    }


    @Override
    public SpanRecorder getSpanRecorder() {
        return trace.getSpanRecorder();
    }

    @Override
    public SpanEventRecorder currentSpanEventRecorder() {
        return trace.currentSpanEventRecorder();
    }

    @Override
    public int getCallStackFrameId() {
        return trace.getCallStackFrameId();
    }

    @Override
    public TraceScope getScope(String name) {
        return trace.getScope(name);
    }

    @Override
    public TraceScope addScope(String name) {
        return trace.addScope(name);
    }

    @Override
    public String toString() {
        return "AsyncTrace{" +
                "traceRoot=" + traceRoot +
                ", trace=" + trace +
                ", entryPoint=" + entryPoint +
                ", asyncId=" + asyncId +
                ", asyncSequence=" + asyncSequence +
                ", asyncState=" + asyncState +
                '}';
    }
}