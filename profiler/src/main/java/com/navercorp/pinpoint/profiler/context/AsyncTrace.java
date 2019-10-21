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

    private static final Logger logger = LoggerFactory.getLogger(AsyncTrace.class.getName());
    private static final boolean isDebug = logger.isDebugEnabled();

    private final TraceRoot traceRoot;
    private final DefaultTrace trace;

    private final AsyncState asyncState;

    public AsyncTrace(final TraceRoot traceRoot, final DefaultTrace trace, final AsyncState asyncState) {
        this.traceRoot = Assert.requireNonNull(traceRoot, "traceRoot");
        this.trace = Assert.requireNonNull(trace, "trace");
        this.asyncState = Assert.requireNonNull(asyncState, "asyncState");
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
        return trace.traceBlockBegin();
    }

    @Override
    public SpanEventRecorder traceBlockBegin(int stackId) {
        return trace.traceBlockBegin(stackId);
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
        return false;
    }

    @Override
    public boolean isRootStack() {
        return this.trace.isRootStack();
    }

    @Override
    public boolean isClosed() {
        return this.trace.isClosed();
    }

    @Override
    public void close() {
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
                ", asyncState=" + asyncState +
                '}';
    }
}