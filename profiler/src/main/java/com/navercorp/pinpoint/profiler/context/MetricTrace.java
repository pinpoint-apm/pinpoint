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

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.context.AsyncTraceId;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.common.AnnotationKey;
import com.navercorp.pinpoint.common.ServiceType;
import com.navercorp.pinpoint.common.util.DefaultParsingResult;
import com.navercorp.pinpoint.common.util.ParsingResult;
import com.navercorp.pinpoint.exception.PinpointException;

/**
 * @author emeroad
 */
public class MetricTrace implements Trace {

    private static final Logger logger = LoggerFactory.getLogger(MetricTrace.class.getName());
    private static final boolean isDebug = logger.isDebugEnabled();
    private static final boolean isTrace = logger.isTraceEnabled();
    
    private static final int EXCEPTION_MARK = -1;

    private static final ParsingResult PARSING_RESULT = new DefaultParsingResult("", new StringBuilder());

    private final boolean sampling = false;

    private TraceId traceId;

    private final CallStack callStack;

    private final TraceContext traceContext;

    private final Map<String, Object> attributeMap = new HashMap<String, Object>();

    // use for calculating depth of each Span.
    private int latestStackIndex = -1;
    private StackFrame currentStackFrame;

    private long transactionId;

    public MetricTrace(final TraceContext traceContext, long transactionId) {
        if (traceContext == null) {
            throw new NullPointerException("traceContext must not be null");
        }
        this.traceContext = traceContext;
        this.transactionId = transactionId;

        final Span span = createSpan();
        this.callStack = new CallStack(span);
        this.latestStackIndex = this.callStack.push();

        final StackFrame stackFrame = createSpanStackFrame(ROOT_STACKID, callStack.getSpan());
        this.callStack.setStackFrame(stackFrame);
        this.currentStackFrame = stackFrame;

    }

    private Span createSpan() {
        return new Span();
    }

    public MetricTrace(TraceContext traceContext, TraceId continueTraceId) {
        if (traceContext == null) {
            throw new NullPointerException("traceContext must not be null");
        }
        if (continueTraceId == null) {
            throw new NullPointerException("continueTraceId must not be null");
        }
        this.traceContext = traceContext;
        this.traceId = continueTraceId;
        final Span span = createSpan();
        this.callStack = new CallStack(span);
        latestStackIndex = this.callStack.push();
        StackFrame stackFrame = createSpanStackFrame(ROOT_STACKID, callStack.getSpan());
        this.callStack.setStackFrame(stackFrame);
        this.currentStackFrame = stackFrame;
    }

    public CallStack getCallStack() {
        return callStack;
    }



    public int getCallStackDepth() {
        return this.callStack.getIndex();
    }


    private StackFrame createSpanEventStackFrame(int stackId) {
        SpanEvent spanEvent = new SpanEvent(callStack.getSpan());
        
        // Set properties for the case when stackFrame is not used as part of Span.
        SpanEventStackFrame stackFrame = new SpanEventStackFrame(spanEvent);
        stackFrame.setStackFrameId(stackId);

        return stackFrame;
    }

    private StackFrame createSpanStackFrame(int stackId, Span span) {
        RootStackFrame stackFrame = new RootStackFrame(span);
        stackFrame.setStackFrameId(stackId);
        return stackFrame;
    }

    @Override
    public void traceBlockBegin() {
        traceBlockBegin(DEFAULT_STACKID);
    }

    @Override
    public void markBeforeTime() {
        this.currentStackFrame.markBeforeTime();
    }

    @Override
    public long getBeforeTime() {
        return this.currentStackFrame.getBeforeTime();
    }

    @Override
    public void markAfterTime() {
        this.currentStackFrame.markAfterTime();
    }

    @Override
    public long getAfterTime() {
        return this.currentStackFrame.getAfterTime();
    }


    @Override
    public void traceBlockBegin(final int stackId) {
        final int currentStackIndex = callStack.push();
        final StackFrame stackFrame = createSpanEventStackFrame(stackId);

        if (latestStackIndex != currentStackIndex) {
            latestStackIndex = currentStackIndex;
            SpanEvent spanEvent = ((SpanEventStackFrame) stackFrame).getSpanEvent();
            spanEvent.setDepth(latestStackIndex);
        }

        callStack.setStackFrame(stackFrame);
        this.currentStackFrame = stackFrame;
    }

    @Override
    public void traceRootBlockEnd() {
        metricResponseTime();
        checkStackId(ROOT_STACKID);
        callStack.popRoot();

        // If the stack is not handled properly, NullPointerException will be thrown after this. Is it OK?
        this.currentStackFrame = null;
    }



    @Override
    public void traceBlockEnd() {
        traceBlockEnd(DEFAULT_STACKID);
    }

    private void metricResponseTime() {
        final int errCode = this.getCallStack().getSpan().getErrCode();
        if (errCode != 0) {
            traceContext.recordContextMetricIsError();
        } else {
            final int elapsedTime = this.currentStackFrame.getElapsedTime();
            traceContext.recordContextMetric(elapsedTime);
        }
    }


    @Override
    public void traceBlockEnd(int stackId) {
        checkStackId(stackId);
        StackFrame popStackFrame = callStack.pop();
        // When pop, current frame have to be recovered.
        this.currentStackFrame = popStackFrame;
    }

    private void checkStackId(int stackId) {
        final StackFrame currentStackFrame = this.currentStackFrame;
        int stackFrameId = currentStackFrame.getStackFrameId();
        if (stackFrameId != stackId) {
            // stack dump will make debugging easy.
            if (logger.isWarnEnabled()) {
                PinpointException exception = new PinpointException("Corrupted CallStack found");
                logger.warn("Corrupted CallStack found. StackId not matched. expected:{} current:{}", stackId, stackFrameId, exception);
            }
        }
    }

    public StackFrame getCurrentStackFrame() {
        return callStack.getCurrentStackFrame();
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
    public boolean canSampled() {
        return this.sampling;
    }

    @Override
    public boolean isRoot() {
        return false;
    }


    @Override
    public void recordException(Throwable th) {
        if (th == null) {
            return;
        }
        // TODO We'd better having MARK Exception to prevent more objects being created.
        this.currentStackFrame.setExceptionInfo(EXCEPTION_MARK, "");

        final Span span = getCallStack().getSpan();
        if (!span.isSetErrCode()) {
            span.setErrCode(1);
        }
    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor) {
    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor, Object[] args) {
    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor, Object args, int index) {
    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor, Object[] args, int start, int end) {
    }

    @Override
    public void recordApiCachedString(MethodDescriptor methodDescriptor, String args, int index) {
    }


    @Override
    public ParsingResult recordSqlInfo(String sql) {
        return PARSING_RESULT;
    }

    @Override
    public void recordSqlParsingResult(ParsingResult parsingResult) {
    }

    @Override
    public void recordSqlParsingResult(ParsingResult parsingResult, String bindValue) {

    }


    @Override
    public void recordAttribute(final AnnotationKey key, final String value) {
    }

    @Override
    public void recordAttribute(final AnnotationKey key, final int value) {
    }


    @Override
    public void recordAttribute(final AnnotationKey key, final Object value) {
    }


    @Override
    public void recordServiceType(final ServiceType serviceType) {
        final StackFrame currentStackFrame = this.currentStackFrame;
        currentStackFrame.setServiceType(serviceType.getCode());
    }

    @Override
    public void recordRpcName(final String rpc) {
    }

    @Override
    public void recordDestinationId(final String destinationId) {
        StackFrame currentStackFrame = this.currentStackFrame;
        if (currentStackFrame instanceof SpanEventStackFrame) {
            ((SpanEventStackFrame) currentStackFrame).setDestinationId(destinationId);
        } else {
            throw new PinpointTraceException("not SpanEventStackFrame");
        }
    }

    @Override
    public void recordEndPoint(final String endPoint) {
    }

    @Override
    public void recordRemoteAddress(final String remoteAddress) {
    }

    @Override
    public void recordNextSpanId(long nextSpanId) {
    }

    @Override
    public void recordParentApplication(String parentApplicationName, short parentApplicationType) {
        StackFrame currentStackFrame = this.currentStackFrame;
        if (currentStackFrame instanceof RootStackFrame) {
            final Span span = ((RootStackFrame) currentStackFrame).getSpan();
            span.setParentApplicationName(parentApplicationName);
            span.setParentApplicationType(parentApplicationType);
            if (isDebug) {
                logger.debug("ParentApplicationName marked. parentApplicationName={}", parentApplicationName);
            }
        } else {
            throw new PinpointTraceException("not RootStackFrame");
        }
    }

    @Override
    public void recordAcceptorHost(String host) {
        StackFrame currentStackFrame = this.currentStackFrame;
        if (currentStackFrame instanceof RootStackFrame) {
            Span span = ((RootStackFrame) currentStackFrame).getSpan();
            span.setAcceptorHost(host); // me
            if (isDebug) {
                logger.debug("Acceptor host received. host={}", host);
            }
        } else {
            throw new PinpointTraceException("not RootStackFrame");
        }
    }

    @Override
    public int getStackFrameId() {
        return this.getCurrentStackFrame().getStackFrameId();
    }
    
    @Override
    public short getServiceType() {
        return currentStackFrame.getServiceType();
    }
    
    @Override
    public Object getAttribute(String key) {
        return attributeMap.get(key);
    }

    @Override
    public Object setAttribute(String key, Object value) {
        return attributeMap.put(key, value);
    }

    @Override
    public Object removeAttribute(String key) {
        return attributeMap.remove(key);
    }
    
    @Override
    public Object setTraceBlockAttachment(Object attachment) {
        return currentStackFrame.attachFrameObject(attachment);
    }

    @Override
    public Object getTraceBlockAttachment() {
        return currentStackFrame.getFrameObject();
    }

    @Override
    public Object removeTraceBlockAttachment() {
        return currentStackFrame.detachFrameObject();
    }

    @Override
    public void recordAsyncId(int asyncId) {
    }

    @Override
    public void recordNextAsyncId(int asyncId) {
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public long getTraceStartTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRootStack() {
        throw new UnsupportedOperationException();
    }

    @Override
    public AsyncTraceId getAsyncTraceId() {
        throw new UnsupportedOperationException();
    }    
}
