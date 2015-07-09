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
import com.navercorp.pinpoint.bootstrap.context.CallStackFrame;
import com.navercorp.pinpoint.bootstrap.context.RootCallStackFrame;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.util.StringUtils;
import com.navercorp.pinpoint.common.trace.AnnotationKey;
import com.navercorp.pinpoint.common.trace.HistogramSchema;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.ParsingResult;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.context.storage.Storage;
import com.navercorp.pinpoint.thrift.dto.TIntStringStringValue;

/**
 * @author netspider
 * @author emeroad
 * @author jaehong.kim
 */
public final class DefaultTrace implements Trace {
    private static final Logger logger = LoggerFactory.getLogger(DefaultTrace.class.getName());
    private static final boolean isDebug = logger.isDebugEnabled();
    private static final boolean isTrace = logger.isTraceEnabled();

    private short sequence;

    private final boolean sampling;

    private final TraceId traceId;

    private final Span span = new Span();
    
    private final CallStack callStack = new CallStack();

    private Storage storage;

    private final TraceContext traceContext;

    // use for calculating depth of each Span.
    private int latestStackIndex = 0;
    private WrappedCallStackFrameHolder recorderHolder = new WrappedCallStackFrameHolder();

    public DefaultTrace(final TraceContext traceContext, long transactionId, boolean sampling) {
        if (traceContext == null) {
            throw new NullPointerException("traceContext must not be null");
        }
        this.traceContext = traceContext;
        this.traceId = new DefaultTraceId(traceContext.getAgentId(), traceContext.getAgentStartTime(), transactionId);
        this.sampling = sampling;

        init();
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

        init();
    }

    private void init() {
        traceId.incrementTraceCount();

        span.setAgentId(traceContext.getAgentId());
        span.setApplicationName(traceContext.getApplicationName());
        span.setAgentStartTime(traceContext.getAgentStartTime());
        span.setApplicationServiceType(traceContext.getServerTypeCode());
        // have to recode traceId latter.
        span.recordTraceId(traceId);
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public Storage getStorage() {
        return this.storage;
    }

    public Span getSpan() {
        return span;
    }
    
    private short nextSequence() {
        return sequence++;
    }

    @Override
    public CallStackFrame traceBlockBegin() {
        return traceBlockBegin(DEFAULT_STACKID);
    }

    @Override
    public CallStackFrame traceBlockBegin(final int stackId) {
        final SpanEvent spanEvent = createSpanEvent(stackId);
        final int currentStackIndex = callStack.push(spanEvent);
        if (latestStackIndex != currentStackIndex) {
            latestStackIndex = currentStackIndex;
            spanEvent.setDepth(latestStackIndex);
        }
        
        return recorderHolder.get(traceContext, spanEvent);
    }

    private SpanEvent createSpanEvent(int stackId) {
        final SpanEvent spanEvent = new SpanEvent(span);
        // Set properties for the case when stackFrame is not used as part of Span.
        spanEvent.setStackId(stackId);
        spanEvent.setSequence(nextSequence());

        return spanEvent;
    }

    @Override
    public void traceBlockEnd() {
        traceBlockEnd(DEFAULT_STACKID);
    }

    @Override
    public void traceBlockEnd(int stackId) {
        pop(stackId);
    }

    private void pop(int stackId) {
        final SpanEvent spanEvent = callStack.pop();
        if (spanEvent == null) {
            if (logger.isWarnEnabled()) {
                PinpointException exception = new PinpointException("Corrupted CallStack found");
                logger.warn("Corrupted CallStack found. stack is empty. expected:{} current:null", stackId, exception);
            }
        }

        if (spanEvent.getStackId() != stackId) {
            // stack dump will make debugging easy.
            if (logger.isWarnEnabled()) {
                PinpointException exception = new PinpointException("Corrupted CallStack found");
                logger.warn("Corrupted CallStack found. StackId not matched. expected:{} current:{}", stackId, spanEvent.getStackId(), exception);
            }
        }

        logSpan(spanEvent);
    }

    @Override
    public void close() {
        // TODO check closed ?

        if (!callStack.empty()) {
            PinpointException exception = new PinpointException("Corrupted CallStack found");
            logger.warn("Corrupted CallStack found. stack is not empty.", exception);
        }
        logSpan(span);

        // If the stack is not handled properly, NullPointerException will be thrown after this. Is it OK?
        if (this.storage != null) {
            this.traceId.decrementTraceCount();
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
    
    public boolean canSampled() {
        return this.sampling;
    }

    public boolean isRoot() {
        return getTraceId().isRoot();
    }

    private void logSpan(SpanEvent spanEvent) {
        if (isTrace) {
            final Thread th = Thread.currentThread();
            logger.trace("[WRITE SpanEvent]{} Thread ID={} Name={}", spanEvent, th.getId(), th.getName());
        }
        this.storage.store(spanEvent);
    }

    private void logSpan(Span span) {
        if (isTrace) {
            final Thread th = Thread.currentThread();
            logger.trace("[WRITE SpanEvent]{} Thread ID={} Name={}", span, th.getId(), th.getName());
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
        return new DefaultAsyncTraceId(traceId, traceContext.getAsyncId(), span.getStartTime());
    }

    @Override
    public RootCallStackFrame rootCallStackFrame() {
        return recorderHolder.get(traceContext, span, traceId, sampling);
    }

    @Override
    public CallStackFrame currentCallStackFrame() {
        final SpanEvent spanEvent = callStack.peek();
        if(spanEvent == null) {
            throw new PinpointException("not found SpanEvent stack");
        }
        
        return recorderHolder.get(traceContext, spanEvent);
    }

    @Override
    public int getCallStackFrameId() {
        if(callStack.empty()) {
            return ROOT_STACKID;
        }
        
        final SpanEvent spanEvent = callStack.peek();
        return spanEvent.getStackId();
    }
}