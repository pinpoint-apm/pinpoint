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

import com.navercorp.pinpoint.bootstrap.context.*;
import com.navercorp.pinpoint.bootstrap.context.scope.TraceScope;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceHandle;
import com.navercorp.pinpoint.profiler.context.id.TraceRoot;
import com.navercorp.pinpoint.profiler.context.id.TraceRootSupport;
import com.navercorp.pinpoint.profiler.context.recorder.WrappedSpanEventRecorder;
import com.navercorp.pinpoint.profiler.context.scope.DefaultTraceScopePool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.context.storage.Storage;

/**
 * @author netspider
 * @author emeroad
 * @author jaehong.kim
 */
public final class DefaultTrace implements Trace, TraceRootSupport {

    private static final Logger logger = LoggerFactory.getLogger(DefaultTrace.class.getName());
    private static final boolean isWarn = logger.isWarnEnabled();

    private final boolean sampling;

    private final CallStack callStack;

    private final Storage storage;

    private final Span span;
    private final SpanRecorder spanRecorder;
    private final WrappedSpanEventRecorder wrappedSpanEventRecorder;

    private final AsyncContextFactory asyncContextFactory;

    private final ActiveTraceHandle activeTraceHandle;

    private boolean closed = false;

    private final DefaultTraceScopePool scopePool = new DefaultTraceScopePool();


    public DefaultTrace(Span span, CallStack callStack, Storage storage, AsyncContextFactory asyncContextFactory, boolean sampling,
                        SpanRecorder spanRecorder, WrappedSpanEventRecorder wrappedSpanEventRecorder, ActiveTraceHandle activeTraceHandle) {

        this.span = Assert.requireNonNull(span, "span must not be null");
        this.callStack = Assert.requireNonNull(callStack, "callStack must not be null");
        this.storage = Assert.requireNonNull(storage, "storage must not be null");
        this.sampling = Assert.requireNonNull(sampling, "sampling must not be null");
        this.asyncContextFactory = Assert.requireNonNull(asyncContextFactory, "asyncContextFactory must not be null");

        this.spanRecorder = Assert.requireNonNull(spanRecorder, "spanRecorder must not be null");
        this.wrappedSpanEventRecorder = Assert.requireNonNull(wrappedSpanEventRecorder, "wrappedSpanEventRecorder must not be null");
        this.activeTraceHandle = Assert.requireNonNull(activeTraceHandle, "activeTraceHandle must not be null");

        setCurrentThread();
    }


    @Override
    public TraceRoot getTraceRoot() {
        return this.span.getTraceRoot();
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
        if (isClosed()) {
            final SpanEvent dummy = newSpanEvent(stackId);
            return wrappedSpanEventRecorder(this.wrappedSpanEventRecorder, dummy);
        }
        // Set properties for the case when stackFrame is not used as part of Span.
        final SpanEvent spanEvent = newSpanEvent(stackId);
        this.callStack.push(spanEvent);
        return wrappedSpanEventRecorder(this.wrappedSpanEventRecorder, spanEvent);
    }

    private SpanEvent newSpanEvent(int stackId) {
        final SpanEvent spanEvent = new SpanEvent(getTraceRoot());
        spanEvent.markStartTime();
        spanEvent.setStackId(stackId);
        return spanEvent;
    }

    private boolean isClosed() {
        if (this.closed) {
            if (isWarn) {
                stackDump("already closed trace.");
            }
            return true;
        }
        return false;
    }

    private void stackDump(String caused) {
        PinpointException exception = new PinpointException(caused);
        logger.warn("[DefaultTrace] Corrupted call stack found. TraceRoot:{}, CallStack:{}", getTraceRoot(), callStack, exception);
    }

    @Override
    public void traceBlockEnd() {
        traceBlockEnd(DEFAULT_STACKID);
    }

    @Override
    public void traceBlockEnd(int stackId) {
        if (isClosed()) {
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

    @Override
    public void close() {
        if (closed) {
            logger.warn("Already closed trace.");
            return;
        }
        closed = true;

        final long afterTime = System.currentTimeMillis();
        if (!callStack.empty()) {
            if (isWarn) {
                stackDump("not empty call stack.");
            }
            // skip
        } else {
            if (span.isTimeRecording()) {
                span.markAfterTime(afterTime);
            }
            logSpan(span);
        }

        this.storage.close();

        purgeActiveTrace(afterTime);
    }

    private void purgeActiveTrace(long currentTime) {
        final ActiveTraceHandle copy = this.activeTraceHandle;
        if (copy != null) {
            copy.purge(currentTime);
        }
    }

    void flush() {
        this.storage.flush();
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
        return span.getStartTime();
    }

    @Override
    public Thread getBindThread() {
        return null;
    }

    @Override
    public long getThreadId() {
        return getTraceRoot().getShared().getThreadId();
    }

    private void setCurrentThread() {
        final long threadId = Thread.currentThread().getId();
        this.setBindThread(threadId);
    }

    private void setBindThread(long threadId) {
        getTraceRoot().getShared().setThreadId(threadId);
    }


    public boolean canSampled() {
        return this.sampling;
    }

    public boolean isRoot() {
        return getTraceId().isRoot();
    }

    private void logSpan(SpanEvent spanEvent) {
        this.storage.store(spanEvent);
    }

    private void logSpan(Span span) {
        this.storage.store(span);
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
    public AsyncTraceId getAsyncTraceId() {
        return asyncContextFactory.newAsyncTraceId(getTraceRoot());
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
            spanEvent = new SpanEvent(getTraceRoot());
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
        return "DefaultTrace{" +
                "sampling=" + sampling +
                ", traceRoot=" + getTraceRoot() +
                '}';
    }
}