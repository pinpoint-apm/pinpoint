/*
 * Copyright 2018 NAVER Corp.
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

import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.common.annotations.VisibleForTesting;
import java.util.Objects;

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
    private static final boolean isDebug = logger.isDebugEnabled();

    private final CallStack<SpanEvent> callStack;

    private final Storage storage;

    private final SpanRecorder spanRecorder;
    private final WrappedSpanEventRecorder wrappedSpanEventRecorder;

    private boolean closed = false;
    // lazy initialize
    private DefaultTraceScopePool scopePool;

    private final TraceRoot traceRoot;
    private final LocalAsyncId localAsyncId;

    public AsyncChildTrace(final TraceRoot traceRoot, CallStack<SpanEvent> callStack, Storage storage, boolean sampling,
                             SpanRecorder spanRecorder, WrappedSpanEventRecorder wrappedSpanEventRecorder, final LocalAsyncId localAsyncId) {

        this.traceRoot = Objects.requireNonNull(traceRoot, "traceRoot");
        this.callStack = Objects.requireNonNull(callStack, "callStack");
        this.storage = Objects.requireNonNull(storage, "storage");
        Assert.isTrue(sampling, "sampling must be true");

        this.spanRecorder = Objects.requireNonNull(spanRecorder, "spanRecorder");
        this.wrappedSpanEventRecorder = Objects.requireNonNull(wrappedSpanEventRecorder, "wrappedSpanEventRecorder");

        this.localAsyncId = Objects.requireNonNull(localAsyncId, "localAsyncId");
        traceBlockBegin(ASYNC_BEGIN_STACK_ID);
    }


    private TraceRoot getTraceRoot() {
        return this.traceRoot;
    }

    private SpanEventRecorder wrappedSpanEventRecorder(WrappedSpanEventRecorder wrappedSpanEventRecorder, SpanEvent spanEvent) {
        wrappedSpanEventRecorder.setWrapped(spanEvent);
        return wrappedSpanEventRecorder;
    }

    @Override
    public SpanEventRecorder traceBlockBegin() {
        return traceBlockBegin(DEFAULT_STACKID);
    }


    @Override
    public SpanEventRecorder traceBlockBegin(final int stackId) {
        final SpanEvent spanEvent = traceBlockBegin0(stackId);
        return wrappedSpanEventRecorder(wrappedSpanEventRecorder, spanEvent);
    }

    private SpanEvent traceBlockBegin0(final int stackId) {
        if (closed) {
            if (logger.isWarnEnabled()) {
                stackDump("already closed trace");
            }
            final SpanEvent dummy = dummySpanEvent();
            return dummy;
        }
        // Set properties for the case when stackFrame is not used as part of Span.
        final SpanEvent spanEvent = newSpanEvent(stackId);
        this.callStack.push(spanEvent);
        return spanEvent;
    }

    private void stackDump(String caused) {
        PinpointException exception = new PinpointException(caused);
        logger.warn("[DefaultTrace] Corrupted call stack found TraceRoot:{}, CallStack:{}", getTraceRoot(), callStack, exception);
    }

    @Override
    public void traceBlockEnd() {
        traceBlockEnd(DEFAULT_STACKID);
    }

    @Override
    public void traceBlockEnd(int stackId) {
        if (closed) {
            if (logger.isWarnEnabled()) {
                stackDump("already closed trace");
            }
            return;
        }

        final SpanEvent spanEvent = callStack.pop();
        if (spanEvent == null) {
            if (logger.isWarnEnabled()) {
                stackDump("call stack is empty.");
            }
            return;
        }

        if (isDummySpanEvent(spanEvent)) {
            if (isDebug) {
                logger.debug("[{}] Skip dummy spanEvent", this);
            }
            return;
        }

        if (spanEvent.getStackId() != stackId) {
            // stack dump will make debugging easy.
            if (logger.isWarnEnabled()) {
                stackDump("not matched stack id. expected=" + stackId + ", current=" + spanEvent.getStackId());
            }
        }

        if (spanEvent.isTimeRecording()) {
            spanEvent.markAfterTime();
        }
        logSpan(spanEvent);
        // state restore
        final SpanEvent previous = callStack.peek();
        wrappedSpanEventRecorder.setWrapped(previous);
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
            if (this.logger.isWarnEnabled()) {
                logger.warn("Already closed {}", this);
            }
            return;
        }
        closed = true;

        if (!callStack.empty()) {
            if (logger.isWarnEnabled()) {
                stackDump("not empty call stack");
            }
            // skip
        } else {
            logSpan();
        }

        this.storage.close();

    }

    @Override
    public TraceId getTraceId() {
        return getTraceRoot().getTraceId();
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
    public boolean canSampled() {
        return true;
    }

    @Override
    public boolean isRoot() {
        return this.getTraceId().isRoot();
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

    @Override
    public SpanRecorder getSpanRecorder() {
        return spanRecorder;
    }

    @Override
    public SpanEventRecorder currentSpanEventRecorder() {
        SpanEvent spanEvent = callStack.peek();
        if (spanEvent == null) {
            if (logger.isWarnEnabled()) {
                stackDump("call stack is empty");
            }
            // make dummy.
            spanEvent = dummySpanEvent();
        }

        return wrappedSpanEventRecorder(this.wrappedSpanEventRecorder, spanEvent);
    }

    private SpanEvent newSpanEvent(int stackId) {
        final SpanEvent spanEvent = callStack.getFactory().newInstance();
        spanEvent.markStartTime();
        spanEvent.setStackId(stackId);
        return spanEvent;
    }

    @VisibleForTesting
    SpanEvent dummySpanEvent() {
        return callStack.getFactory().dummyInstance();
    }

    @VisibleForTesting
    boolean isDummySpanEvent(final SpanEvent spanEvent) {
        return callStack.getFactory().isDummy(spanEvent);
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
        if (scopePool == null) {
            return null;
        }
        return scopePool.get(name);
    }

    @Override
    public TraceScope addScope(String name) {
        if (scopePool == null) {
            this.scopePool = new DefaultTraceScopePool();
        }
        return scopePool.add(name);
    }

    @Override
    public String toString() {
        return "AsyncChildTrace{" +
                "traceRoot=" + getTraceRoot() +
                ", localAsyncId=" + localAsyncId +
                '}';
    }
}