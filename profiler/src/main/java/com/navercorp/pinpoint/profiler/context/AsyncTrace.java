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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncTrace implements Trace {
    private static final int BEGIN_STACKID = 1;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final Trace trace;
    private final boolean entryPoint;

    private int asyncId;
    private short asyncSequence;
    private AsyncTraceCloser closer;

    public AsyncTrace(final Trace trace, final AsyncTraceCloser closer) {
        if (closer == null) {
            throw new IllegalArgumentException("closer must not be null.");
        }

        this.trace = trace;
        this.closer = closer;
        this.entryPoint = true;
    }

    public AsyncTrace(final Trace trace, final int asyncId, final short asyncSequence, final long startTime) {
        this.trace = trace;
        this.asyncId = asyncId;
        this.asyncSequence = asyncSequence;

        this.closer = null;
        this.entryPoint = false;

        this.trace.getSpanRecorder().recordStartTime(startTime);
        traceBlockBegin(BEGIN_STACKID);
    }

    public int getAsyncId() {
        return asyncId;
    }

    @Override
    public long getId() {
        if (this.entryPoint) {
            return this.trace.getId();
        }

        return -1;
    }

    @Override
    public long getStartTime() {
        if (this.entryPoint) {
            return this.trace.getStartTime();
        }

        return 0;
    }

    @Override
    public Thread getBindThread() {
        if (this.entryPoint) {
            return this.trace.getBindThread();
        }

        return null;
    }

    @Override
    public TraceId getTraceId() {
        return trace.getTraceId();
    }

    @Override
    public boolean canSampled() {
        return trace.canSampled();
    }

    @Override
    public boolean isRoot() {
        return trace.isRoot();
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
        return getAsyncTraceId(false);
    }

    @Override
    public AsyncTraceId getAsyncTraceId(boolean closeable) {
        final AsyncTraceId asyncTraceId = this.trace.getAsyncTraceId();
        if (closeable && this.entryPoint && this.closer != null) {
            this.closer.setup();
            return new CloseableAsyncTraceId(asyncTraceId, this.closer);
        }

        return asyncTraceId;
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
        if (this.closer == null) {
            return;
        }

        if (this.closer.await()) {
            // flush.
            this.trace.flush();
            if (isDebug) {
                logger.debug("Flush trace={}, closer={}", this, this.closer);
            }
        } else {
            // close.
            this.trace.close();
            if (isDebug) {
                logger.debug("Close trace={}. closer={}", this, this.closer);
            }
        }
        this.closer = null;
    }

    @Override
    public void flush() {
        this.trace.flush();
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
}