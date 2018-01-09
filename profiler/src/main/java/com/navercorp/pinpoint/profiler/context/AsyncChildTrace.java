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
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.recorder.WrappedSpanEventRecorder;
import com.navercorp.pinpoint.profiler.context.scope.DefaultTraceScopePool;
import com.navercorp.pinpoint.profiler.context.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncChildTrace implements Trace {

    private static final int ASYNC_BEGIN_STACK_ID = 1001;

    private static final Logger logger = LoggerFactory.getLogger(AsyncChildTrace.class.getName());
    private static final boolean isWarn = logger.isWarnEnabled();

    private final boolean sampling;

    private final CallStack callStack;

    private final Storage storage;

    private final TraceRoot traceRoot;
    private final WrappedSpanEventRecorder wrappedSpanEventRecorder;

    private final AsyncContextFactory asyncContextFactory;
    private final SpanRecorder spanRecorder;

    private boolean closed = false;

    private final DefaultTraceScopePool scopePool = new DefaultTraceScopePool();

    private final int asyncId;
    private final short asyncSequence;

    public AsyncChildTrace(final TraceRoot traceRoot, CallStack callStack, Storage storage, AsyncContextFactory asyncContextFactory, boolean sampling,
                             SpanRecorder spanRecorder, WrappedSpanEventRecorder wrappedSpanEventRecorder, final int asyncId, final short asyncSequence) {

        this.traceRoot = Assert.requireNonNull(traceRoot, "traceRoot must not be null");
        this.callStack = Assert.requireNonNull(callStack, "callStack must not be null");
        this.storage = Assert.requireNonNull(storage, "storage must not be null");
        this.asyncContextFactory = Assert.requireNonNull(asyncContextFactory, "asyncContextFactory must not be null");
        this.sampling = sampling;
        this.spanRecorder = Assert.requireNonNull(spanRecorder, "spanRecorder must not be null");
        this.wrappedSpanEventRecorder = Assert.requireNonNull(wrappedSpanEventRecorder, "wrappedSpanEventRecorder must not be null");
        this.asyncId = asyncId;
        this.asyncSequence = asyncSequence;

        traceBlockBegin(ASYNC_BEGIN_STACK_ID);
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

    private SpanEventRecorder wrappedSpanEventRecorder(WrappedSpanEventRecorder wrappedSpanEventRecorder, SpanEvent spanEvent) {
        wrappedSpanEventRecorder.setWrapped(spanEvent);
        return wrappedSpanEventRecorder;
    }

    @Override
    public boolean canSampled() {
        return sampling;
    }

    @Override
    public boolean isRoot() {
        return this.traceRoot.getTraceId().isRoot();
    }

    @Override
    public SpanEventRecorder traceBlockBegin() {
        return traceBlockBegin(DEFAULT_STACKID);
    }

    public SpanEvent traceBlockBegin0(final int stackId) {
        if (closed) {
            if (isWarn) {
                stackDump("already closed trace");
            }
            final SpanEvent dummy = newSpanEvent(stackId);
            return dummy;
        }
        // Set properties for the case when stackFrame is not used as part of Span.
        final SpanEvent spanEvent = newSpanEvent(stackId);
        this.callStack.push(spanEvent);
        return spanEvent;
    }

    private SpanEvent newSpanEvent(int stackId) {
        final SpanEvent spanEvent = new SpanEvent(traceRoot);
        spanEvent.markStartTime();
        spanEvent.setStackId(stackId);
        return spanEvent;
    }

    private void stackDump(String caused) {
        PinpointException exception = new PinpointException(caused);
        logger.warn("[DefaultTrace] Corrupted call stack found TraceRoot:{}, CallStack:{}", traceRoot, callStack, exception);
    }

    @Override
    public SpanEventRecorder traceBlockBegin(int stackId) {
        final SpanEvent spanEvent = traceBlockBegin0(stackId);
        spanEvent.setAsyncId(asyncId);
        spanEvent.setAsyncSequence(asyncSequence);

        return wrappedSpanEventRecorder(wrappedSpanEventRecorder, spanEvent);
    }

    @Override
    public void traceBlockEnd() {
        traceBlockEnd(DEFAULT_STACKID);
    }

    @Override
    public void traceBlockEnd(int stackId) {
        if (closed) {
            if (isWarn) {
                stackDump("already closed trace");
            }
            return;
        }

        final SpanEvent spanEvent = callStack.pop();
        if (spanEvent == null) {
            if (isWarn) {
                stackDump("call stack is empty.");
            }
            return;
        }

        if (spanEvent.getStackId() != stackId) {
            // stack dump will make debugging easy.
            if (isWarn) {
                stackDump("not matched stack id. expected=" + stackId + ", current=" + spanEvent.getStackId());
            }
        }

        if (spanEvent.isTimeRecording()) {
            spanEvent.markAfterTime();
        }
        logSpan(spanEvent);
    }

    private void logSpan(SpanEvent spanEvent) {
        this.storage.store(spanEvent);
    }

    private void logSpan() {
        this.storage.flush();
    }

    @Override
    public boolean isAsync() {
        return true;
    }

    @Override
    public boolean isRootStack() {
        return getCallStackFrameId0() == ASYNC_BEGIN_STACK_ID;
    }

    public int getCallStackFrameId0() {
        final SpanEvent spanEvent = callStack.peek();
        if (spanEvent == null) {
            return ROOT_STACKID;
        } else {
            return spanEvent.getStackId();
        }
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
        return closed;
    }

    @Override
    public void close() {
        traceBlockEnd(ASYNC_BEGIN_STACK_ID);
        close0();
    }

    public void close0() {
        if (closed) {
            logger.warn("Already closed childTrace");
            return;
        }
        closed = true;

        if (!callStack.empty()) {
            if (isWarn) {
                stackDump("not empty call stack");
            }
            // skip
        } else {
            logSpan();
        }

        this.storage.close();

    }


    @Override
    public SpanRecorder getSpanRecorder() {
        return spanRecorder;
    }

    @Override
    public SpanEventRecorder currentSpanEventRecorder() {
        SpanEvent spanEvent = callStack.peek();
        if (spanEvent == null) {
            if (isWarn) {
                stackDump("call stack is empty");
            }
            // make dummy.
            spanEvent = new SpanEvent(traceRoot);
        }

        return wrappedSpanEventRecorder(this.wrappedSpanEventRecorder, spanEvent);
    }

    @Override
    public int getCallStackFrameId() {
        final SpanEvent spanEvent = callStack.peek();
        if (spanEvent == null) {
            return ROOT_STACKID;
        } else {
            return spanEvent.getStackId();
        }
    }

    @Override
    public TraceScope getScope(String name) {
        return scopePool.get(name);
    }

    @Override
    public TraceScope addScope(String name) {
        return scopePool.add(name);
    }

    @Override
    public String toString() {
        return "AsyncChildTrace{" +
                "traceRoot=" + traceRoot +
                ", asyncId=" + asyncId +
                ", asyncSequence=" + asyncSequence +
                '}';
    }
}