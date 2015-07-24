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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.SpanEventRecorder;
import com.navercorp.pinpoint.bootstrap.context.SpanRecorder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.context.TraceType;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.context.storage.Storage;

/**
 * @author netspider
 * @author emeroad
 * @author jaehong.kim
 */
public final class DefaultTrace implements Trace {
    private static final Logger logger = LoggerFactory.getLogger(DefaultTrace.class.getName());
    private static final boolean isTrace = logger.isTraceEnabled();
    private static final boolean isWarn = logger.isWarnEnabled();

    private final boolean sampling;

    private final TraceId traceId;

    private final CallStack callStack;

    private Storage storage;

    private final TraceContext traceContext;
    private TraceType traceType = TraceType.DEFAULT;
    private final WrappedSpanEventRecorder spanEventRecorder;
    private final DefaultSpanRecorder spanRecorder;
    private boolean closed = false;

    private Thread bindThread;

    public DefaultTrace(final TraceContext traceContext, long transactionId, boolean sampling) {
        if (traceContext == null) {
            throw new NullPointerException("traceContext must not be null");
        }
        this.traceContext = traceContext;
        this.traceId = new DefaultTraceId(traceContext.getAgentId(), traceContext.getAgentStartTime(), transactionId);
        this.sampling = sampling;

        final Span span = createSpan();
        this.spanRecorder = new DefaultSpanRecorder(traceContext, span, traceId, sampling);
        this.spanRecorder.recordTraceId(traceId);
        this.spanEventRecorder = new WrappedSpanEventRecorder(traceContext);
        if (traceContext.getProfilerConfig() != null) {
            final int maxCallStackDepth = traceContext.getProfilerConfig().getCallStackMaxDepth();
            this.callStack = new CallStack(span, maxCallStackDepth);
        } else {
            this.callStack = new CallStack(span);
        }
        setCurrentThread();
    }

    public DefaultTrace(TraceContext traceContext, TraceId continueTraceId, boolean sampling) {
        if (traceContext == null) {
            throw new NullPointerException("traceContext must not be null");
        }
        if (continueTraceId == null) {
            throw new NullPointerException("continueTraceId must not be null");
        }
        this.traceContext = traceContext;
        this.traceId = continueTraceId;
        this.sampling = sampling;

        final Span span = createSpan();
        this.spanRecorder = new DefaultSpanRecorder(traceContext, span, traceId, sampling);
        this.spanRecorder.recordTraceId(traceId);
        this.spanEventRecorder = new WrappedSpanEventRecorder(traceContext);
        if (traceContext.getProfilerConfig() != null) {
            final int maxCallStackDepth = traceContext.getProfilerConfig().getCallStackMaxDepth();
            this.callStack = new CallStack(span, maxCallStackDepth);
        } else {
            this.callStack = new CallStack(span);
        }
        setCurrentThread();
    }

    private Span createSpan() {
        Span span = new Span();
        span.setAgentId(traceContext.getAgentId());
        span.setApplicationName(traceContext.getApplicationName());
        span.setAgentStartTime(traceContext.getAgentStartTime());
        span.setApplicationServiceType(traceContext.getServerTypeCode());
        span.markBeforeTime();

        return span;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    @Override
    public SpanEventRecorder traceBlockBegin() {
        return traceBlockBegin(DEFAULT_STACKID);
    }

    @Override
    public SpanEventRecorder traceBlockBegin(final int stackId) {
        // Set properties for the case when stackFrame is not used as part of Span.
        final SpanEvent spanEvent = new SpanEvent(spanRecorder.getSpan());
        spanEvent.markStartTime();
        spanEvent.setStackId(stackId);

        if (this.closed) {
            if (isWarn) {
                PinpointException exception = new PinpointException("already closed trace.");
                logger.warn("[DefaultTrace] Corrupted call stack found.", exception);
            }
        } else {
            callStack.push(spanEvent);
        }

        spanEventRecorder.setWrapped(spanEvent);
        return spanEventRecorder;
    }

    @Override
    public void traceBlockEnd() {
        traceBlockEnd(DEFAULT_STACKID);
    }

    @Override
    public void traceBlockEnd(int stackId) {
        if (this.closed) {
            if (isWarn) {
                final PinpointException exception = new PinpointException("already closed trace.");
                logger.warn("[DefaultTrace] Corrupted call stack found.", exception);
            }
            return;
        }

        final SpanEvent spanEvent = callStack.pop();
        if (spanEvent == null) {
            if (isWarn) {
                PinpointException exception = new PinpointException("call stack is empty.");
                logger.warn("[DefaultTrace] Corrupted call stack found.", exception);
            }
            return;
        }

        if (spanEvent.getStackId() != stackId) {
            // stack dump will make debugging easy.
            if (isWarn) {
                PinpointException exception = new PinpointException("not matched stack id. expected=" + stackId + ", current=" + spanEvent.getStackId());
                logger.warn("[DefaultTrace] Corrupted call stack found.", exception);
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

        if (!callStack.empty()) {
            if (isWarn) {
                PinpointException exception = new PinpointException("not empty call stack.");
                logger.warn("[DefaultTrace] Corrupted call stack found.", exception);
            }
            // skip
        } else {
            Span span = spanRecorder.getSpan();
            if (span.isTimeRecording()) {
                span.markAfterTime();
            }
            logSpan(span);
        }

        // If the stack is not handled properly, NullPointerException will be thrown after this. Is it OK?
        if (this.storage != null) {
            this.storage.close();
            this.storage = null;
        }
    }

    /**
     * Get current TraceID. If it was not set this will return null.
     *
     * @return
     */
    @Override
    public TraceId getTraceId() {
        return this.traceId;
    }

    @Override
    public long getId() {
        return traceId.getTransactionSequence();
    }

    @Override
    public long getStartTime() {
        final DefaultSpanRecorder copy = this.spanRecorder;
        if (copy == null) {
            return 0;
        }
        return copy.getSpan().getStartTime();
    }

    @Override
    public Thread getBindThread() {
        return bindThread;
    }

    private void setCurrentThread() {
        this.setBindThread(Thread.currentThread());
    }

    private void setBindThread(Thread thread) {
        bindThread = thread;
    }


    public boolean canSampled() {
        return this.sampling;
    }

    public boolean isRoot() {
        return getTraceId().isRoot();
    }

    private void logSpan(SpanEvent spanEvent) {
        if (isTrace) {
            final Thread th = Thread.currentThread();
            logger.trace("[DefaultTrace] Write {} thread{id={}, name={}}", spanEvent, th.getId(), th.getName());
        }
        this.storage.store(spanEvent);
    }

    private void logSpan(Span span) {
        if (isTrace) {
            final Thread th = Thread.currentThread();
            logger.trace("[DefaultTrace] Write {} thread{id={}, name={}}", span, th.getId(), th.getName());
        }
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
        return new DefaultAsyncTraceId(traceId, traceContext.getAsyncId(), spanRecorder.getSpan().getStartTime());
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
                PinpointException exception = new PinpointException("call stack is empty");
                logger.warn("[DefaultTrace] Corrupted call stack found.", exception);
            }
            // make dummy.
            spanEvent = new SpanEvent(spanRecorder.getSpan());
        }

        spanEventRecorder.setWrapped(spanEvent);
        return spanEventRecorder;
    }

    @Override
    public int getCallStackFrameId() {
        if (callStack.empty()) {
            return ROOT_STACKID;
        }

        final SpanEvent spanEvent = callStack.peek();
        return spanEvent.getStackId();
    }

    @Override
    public TraceType getTraceType() {
        return this.traceType;
    }

    public void setTraceType(TraceType traceType) {
        this.traceType = traceType;
    }
}