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
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.context.exception.model.ExceptionRecordingContext;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.recorder.WrappedSpanEventRecorder;
import com.navercorp.pinpoint.profiler.context.scope.DefaultTraceScopePool;
import com.navercorp.pinpoint.profiler.context.storage.Storage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * @author netspider
 * @author emeroad
 * @author jaehong.kim
 */
public class DefaultTrace implements Trace {

    protected final Logger logger = LogManager.getLogger(getClass());
    protected final boolean isDebug = logger.isDebugEnabled();

    private final CallStack<SpanEvent> callStack;

    private final Storage storage;

    private final SpanRecorder spanRecorder;
    private final WrappedSpanEventRecorder wrappedSpanEventRecorder;

    private final ExceptionRecordingContext exceptionRecordingContext;

    private boolean closed = false;
    // lazy initialize
    private DefaultTraceScopePool scopePool;

    private final Span span;

    private final CloseListener closeListener;

    public DefaultTrace(Span span, CallStack<SpanEvent> callStack, Storage storage,
                        SpanRecorder spanRecorder, WrappedSpanEventRecorder wrappedSpanEventRecorder,
                        ExceptionRecordingContext exceptionRecordingContext) {
        this(span, callStack, storage, spanRecorder, wrappedSpanEventRecorder, exceptionRecordingContext, CloseListener.EMPTY);
    }

    public DefaultTrace(Span span, CallStack<SpanEvent> callStack, Storage storage,
                        SpanRecorder spanRecorder,
                        WrappedSpanEventRecorder wrappedSpanEventRecorder,
                        ExceptionRecordingContext exceptionRecordingContext,
                        CloseListener closeListener) {

        this.span = Objects.requireNonNull(span, "span");
        this.callStack = Objects.requireNonNull(callStack, "callStack");
        this.storage = Objects.requireNonNull(storage, "storage");

        this.spanRecorder = Objects.requireNonNull(spanRecorder, "spanRecorder");
        this.wrappedSpanEventRecorder = Objects.requireNonNull(wrappedSpanEventRecorder, "wrappedSpanEventRecorder");
        this.exceptionRecordingContext = Objects.requireNonNull(exceptionRecordingContext, "exceptionRecordingContext");

        this.closeListener = closeListener;

        setCurrentThread();
    }

    private void setCurrentThread() {
        final long threadId = Thread.currentThread().getId();
        getTraceRoot().getShared().setThreadId(threadId);
    }

    private TraceRoot getTraceRoot() {
        return this.span.getTraceRoot();
    }

    private SpanEventRecorder wrappedSpanEventRecorder(WrappedSpanEventRecorder wrappedSpanEventRecorder, SpanEvent spanEvent) {
        wrappedSpanEventRecorder.setWrapped(spanEvent, this.exceptionRecordingContext);
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
            return dummySpanEvent();
        }
        // Set properties for the case when stackFrame is not used as part of Span.
        final SpanEvent spanEvent = newSpanEvent(stackId);
        this.callStack.push(spanEvent);
        return spanEvent;
    }

    private void stackDump(String caused) {
        PinpointException exception = new PinpointException(caused);
        logger.warn("Corrupted call stack found TraceRoot:{}, CallStack:{}", getTraceRoot(), callStack, exception);
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
        wrappedSpanEventRecorder(wrappedSpanEventRecorder, previous);
    }


    @Override
    public boolean isClosed() {
        return closed;
    }

    @Override
    public void close() {
        if (closed) {
            logger.warn("Already closed {}", this);
            return;
        }
        closed = true;

        final long afterTime = System.currentTimeMillis();
        if (!callStack.empty()) {
            if (logger.isWarnEnabled()) {
                stackDump("not empty call stack");
            }
            // skip
        } else {
            if (span.isTimeRecording()) {
                span.markAfterTime(afterTime);
            }
            logSpan();
        }

        this.storage.close();
        this.closeListener.close(afterTime);
    }


    protected void flush() {
        this.storage.flush();
        this.closed = true;
    }

    /**
     * Get current TraceID. If it was not set this will return null.
     *
     * @return
     */
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
        this.storage.store(this.span);
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public boolean isRootStack() {
        return callStack.empty();
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
        final SpanEvent spanEvent = callStack.newInstance();
        spanEvent.markStartTime();
        spanEvent.setStackId(stackId);
        return spanEvent;
    }

    @VisibleForTesting
    SpanEvent dummySpanEvent() {
        return callStack.disableInstance();
    }

    @VisibleForTesting
    boolean isDummySpanEvent(final SpanEvent spanEvent) {
        return callStack.isDisable(spanEvent);
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
        return "DefaultTrace{" +
                "traceRoot=" + getTraceRoot() +
                '}';
    }
}