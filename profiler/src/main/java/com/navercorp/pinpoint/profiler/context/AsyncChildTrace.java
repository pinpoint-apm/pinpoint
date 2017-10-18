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

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;

public class AsyncChildTrace implements Trace {

    private static final int BEGIN_STACKID = 1;

    private final AsyncContextFactory asyncContextFactory;

    private final TraceRoot traceRoot;
    private final DefaultTrace trace;

    private final int asyncId;
    private final short asyncSequence;

    public AsyncChildTrace(final AsyncContextFactory asyncContextFactory, final TraceRoot traceRoot, final DefaultTrace trace, final int asyncId, final short asyncSequence) {
        this.asyncContextFactory = Assert.requireNonNull(asyncContextFactory, "asyncContextFactory must not be null");
        this.traceRoot = Assert.requireNonNull(traceRoot, "traceRoot must not be null");
        this.trace = Assert.requireNonNull(trace, "trace must not be null");
        this.asyncId = asyncId;
        this.asyncSequence = asyncSequence;

        traceBlockBegin(BEGIN_STACKID);
    }


    @Override
    public long getId() {
        return traceRoot.getLocalTransactionId();
    }

    @Override
    public long getStartTime() {
        return this.traceRoot.getTraceStartTime();
    }

    @Override
    public Thread getBindThread() {
        return null;
    }

    @Override
    public long getThreadId() {
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
        recorder.recordAsyncId(asyncId);
        recorder.recordAsyncSequence(asyncSequence);
        return recorder;
    }

    @Override
    public SpanEventRecorder traceBlockBegin(int stackId) {
        final SpanEventRecorder recorder = trace.traceBlockBegin(stackId);
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
        return true;
    }

    @Override
    public boolean isRootStack() {
        return trace.getCallStackFrameId() == BEGIN_STACKID;
    }

    /**
     * @deprecated Since 1.7.0 Use {@link SpanEventRecorder#recordNextAsyncContext()}
     * This API will be removed in 1.8.0
     */
    @Deprecated
    @Override
    public AsyncTraceId getAsyncTraceId() {
        return asyncContextFactory.newAsyncTraceId(traceRoot);
    }

    @Override
    public boolean isClosed() {
        return this.trace.isClosed();
    }

    @Override
    public void close() {
        traceBlockEnd(BEGIN_STACKID);
        trace.close();
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
        return "AsyncChildTrace{" +
                "traceRoot=" + traceRoot +
                ", trace=" + trace +
                ", asyncId=" + asyncId +
                ", asyncSequence=" + asyncSequence +
                '}';
    }
}