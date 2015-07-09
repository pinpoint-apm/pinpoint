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
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.context.TraceId;
import com.navercorp.pinpoint.bootstrap.context.TraceType;
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
 */
public final class DefaultTrace implements Trace {

    private static final Logger logger = LoggerFactory.getLogger(DefaultTrace.class.getName());
    private static final boolean isDebug = logger.isDebugEnabled();
    private static final boolean isTrace = logger.isTraceEnabled();

    private short sequence;

    private boolean sampling = true;

    private final TraceId traceId;

    private final CallStack callStack;

    private Storage storage;

    private final TraceContext traceContext;

    // use for calculating depth of each Span.
    private int latestStackIndex = -1;
    private StackFrame currentStackFrame;
    private boolean closed = false;
    private TraceType traceType = TraceType.DEFAULT;

    public DefaultTrace(final TraceContext traceContext, long transactionId) {
        if (traceContext == null) {
            throw new NullPointerException("traceContext must not be null");
        }
        this.traceContext = traceContext;
        this.traceId = new DefaultTraceId(traceContext.getAgentId(), traceContext.getAgentStartTime(), transactionId);
        this.traceId.incrementTraceCount();
        
        final Span span = createSpan(traceId);
        this.callStack = new CallStack(span);
        this.latestStackIndex = this.callStack.push();

        final StackFrame stackFrame = createSpanStackFrame(ROOT_STACKID, callStack.getSpan());
        this.callStack.setStackFrame(stackFrame);
        this.currentStackFrame = stackFrame;
    }

    private Span createSpan(final TraceId traceId) {
        final Span span = new Span();
        span.setAgentId(traceContext.getAgentId());
        span.setApplicationName(traceContext.getApplicationName());
        span.setAgentStartTime(traceContext.getAgentStartTime());
        span.setApplicationServiceType(traceContext.getServerTypeCode());

        // have to recode traceId latter.
        span.recordTraceId(traceId);
        return span;
    }

    public DefaultTrace(TraceContext traceContext, TraceId continueTraceId) {
        if (traceContext == null) {
            throw new NullPointerException("traceContext must not be null");
        }
        if (continueTraceId == null) {
            throw new NullPointerException("continueTraceId must not be null");
        }
        this.traceContext = traceContext;
        this.traceId = continueTraceId;
        this.traceId.incrementTraceCount();
        final Span span = createSpan(continueTraceId);
        this.callStack = new CallStack(span);
        latestStackIndex = this.callStack.push();
        StackFrame stackFrame = createSpanStackFrame(ROOT_STACKID, callStack.getSpan());
        this.callStack.setStackFrame(stackFrame);
        this.currentStackFrame = stackFrame;
    }

    public CallStack getCallStack() {
        return callStack;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }
    
    public Storage getStorage() {
        return this.storage;
    }

    public short nextSequence() {
        return sequence++;
    }

    public int getCallStackDepth() {
        return this.callStack.getIndex();
    }


    private StackFrame createSpanEventStackFrame(int stackId) {
        SpanEvent spanEvent = new SpanEvent(callStack.getSpan());

        // Set properties for the case when stackFrame is not used as part of Span.
        SpanEventStackFrame stackFrame = new SpanEventStackFrame(spanEvent);
        stackFrame.setStackFrameId(stackId);
        stackFrame.setSequence(nextSequence());
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
    public void close() {
        // TODO STATDISABLE remove stat code for now
        // metricResponseTime();
        try {
            pop(ROOT_STACKID);
            callStack.popRoot();
        } catch(Exception e) {
            logger.warn("Failed to close", e);
        }
        
        // If the stack is not handled properly, NullPointerException will be thrown after this. Is it OK?
        this.currentStackFrame = null;
        if(this.storage != null) {
            this.traceId.decrementTraceCount();
            this.storage.close();
            this.storage = null;
        }
    }

    @Override
    public void traceBlockEnd() {
        traceBlockEnd(DEFAULT_STACKID);
    }

    private void metricResponseTime() {
        final Span span = this.getCallStack().getSpan();
        final boolean isError = span.getErrCode() != 0;
        final int elapsedTime = this.currentStackFrame.getElapsedTime();
        final TraceContext traceContext = this.traceContext;
        if (isError) {
            traceContext.recordContextMetricIsError();
        } else {
            traceContext.recordContextMetric(elapsedTime);
        }
        final String parentApplicationName = span.getParentApplicationName();
        if (parentApplicationName == null) {
            if (isError) {
                traceContext.recordUserAcceptResponseTime(HistogramSchema.ERROR_SLOT_TIME);
            } else {
                traceContext.recordUserAcceptResponseTime(elapsedTime);
            }
        } else {
            final short parentApplicationType = span.getParentApplicationType();
            if (isError) {
                traceContext.recordAcceptResponseTime(parentApplicationName, parentApplicationType, HistogramSchema.ERROR_SLOT_TIME);
            } else {
                traceContext.recordAcceptResponseTime(parentApplicationName, parentApplicationType, elapsedTime);
            }
        }
    }

    @Override
    public void traceBlockEnd(int stackId) {
        pop(stackId);
        StackFrame popStackFrame = callStack.pop();
        // When pop, current frame have to be recovered.
        this.currentStackFrame = popStackFrame;
    }

    private void pop(int stackId) {
        final StackFrame currentStackFrame = this.currentStackFrame;
        int stackFrameId = currentStackFrame.getStackFrameId();
        if (stackFrameId != stackId) {
            // stack dump will make debugging easy.
            if (logger.isWarnEnabled()) {
                PinpointException exception = new PinpointException("Corrupted CallStack found");
                logger.warn("Corrupted CallStack found. StackId not matched. expected:{} current:{}", stackId, stackFrameId, exception);
            }
        }
        if (currentStackFrame instanceof RootStackFrame) {
            logSpan(((RootStackFrame) currentStackFrame).getSpan());
        } else {
            logSpan(((SpanEventStackFrame) currentStackFrame).getSpanEvent());
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
    

    public boolean canSampled() {
        return this.sampling;
    }

    public boolean isRoot() {
        return getTraceId().isRoot();
    }

    public void setSampling(boolean sampling) {
        this.sampling = sampling;
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
    public void recordException(Throwable th) {
        if (th == null) {
            return;
        }
        final String drop = StringUtils.drop(th.getMessage(), 256);
        // An exception that is an instance of a proxy class could make something wrong because the class name will vary.
        final int exceptionId = traceContext.cacheString(th.getClass().getName());
        this.currentStackFrame.setExceptionInfo(exceptionId, drop);

        final Span span = getCallStack().getSpan();
        if (!span.isSetErrCode()) {
            span.setErrCode(1);
        }
    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor) {
        if (methodDescriptor == null) {
            return;
        }
        if (methodDescriptor.getApiId() == 0) {
            recordAttribute(AnnotationKey.API, methodDescriptor.getFullName());
        } else {
            recordApiId(methodDescriptor.getApiId());
        }
    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor, Object[] args) {
        // Need to improve the way of storing APIs.                                                                                        
        recordApi(methodDescriptor);
        recordArgs(args);
    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor, Object args, int index) {
        recordApi(methodDescriptor);
        recordSingleArg(args, index);
    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor, Object[] args, int start, int end) {
        recordApi(methodDescriptor);
        recordArgs(args, start, end);
    }

    @Override
    public void recordApiCachedString(MethodDescriptor methodDescriptor, String args, int index) {
        recordApi(methodDescriptor);
        recordSingleCachedString(args, index);
    }


    private void recordArgs(Object[] args, int start, int end) {
        if (args != null) {
            int max = Math.min(Math.min(args.length, AnnotationKey.MAX_ARGS_SIZE), end);
            for (int i = start; i < max; i++) {
                recordAttribute(AnnotationKey.getArgs(i), args[i]);
            }
            // TODO How to handle if args length is greater than MAX_ARGS_SIZE?
        }
    }

    private void recordSingleArg(Object args, int index) {
        if (args != null) {
            recordAttribute(AnnotationKey.getArgs(index), args);
        }
    }

    private void recordSingleCachedString(String args, int index) {
        if (args != null) {
            int cacheId = traceContext.cacheString(args);
            recordAttribute(AnnotationKey.getCachedArgs(index), cacheId);
        }
    }

    private void recordArgs(Object[] args) {
        if (args != null) {
            int max = Math.min(args.length, AnnotationKey.MAX_ARGS_SIZE);
            for (int i = 0; i < max; i++) {
                recordAttribute(AnnotationKey.getArgs(i), args[i]);
            }
         // TODO How to handle if args length is greater than MAX_ARGS_SIZE?                                                                  
        }
    }


    @Override
    public ParsingResult recordSqlInfo(String sql) {
        if (sql == null) {
            return null;
        }
        ParsingResult parsingResult = traceContext.parseSql(sql);
        recordSqlParsingResult(parsingResult);
        return parsingResult;
    }

    @Override
    public void recordSqlParsingResult(ParsingResult parsingResult) {
        recordSqlParsingResult(parsingResult, null);
    }

    @Override
    public void recordSqlParsingResult(ParsingResult parsingResult, String bindValue) {
        if (parsingResult == null) {
            return;
        }
        final boolean isNewCache = traceContext.cacheSql(parsingResult);
        if (isDebug) {
            if (isNewCache) {
                logger.debug("update sql cache. parsingResult:{}", parsingResult);
            } else {
                logger.debug("cache hit. parsingResult:{}", parsingResult);
            }
        }

//        final String sql = parsingResult.getSql();
        final TIntStringStringValue tSqlValue = new TIntStringStringValue(parsingResult.getId());
        final String output = parsingResult.getOutput();
        if (isNotEmpty(output)) {
            tSqlValue.setStringValue1(output);
        }
        if (isNotEmpty(bindValue)) {
            tSqlValue.setStringValue2(bindValue);
        }
        recordSqlParam(tSqlValue);
    }

    private static boolean isNotEmpty(final String bindValue) {
        return bindValue != null && !bindValue.isEmpty();
    }

    private void recordSqlParam(TIntStringStringValue tIntStringStringValue) {
        final StackFrame currentStackFrame = this.currentStackFrame;
        currentStackFrame.addAnnotation(new Annotation(AnnotationKey.SQL_ID.getCode(), tIntStringStringValue));
    }

    @Override
    public void recordAttribute(final AnnotationKey key, final String value) {
        final StackFrame currentStackFrame = this.currentStackFrame;
        currentStackFrame.addAnnotation(new Annotation(key.getCode(), value));
    }

    @Override
    public void recordAttribute(final AnnotationKey key, final int value) {
        final StackFrame currentStackFrame = this.currentStackFrame;
        currentStackFrame.addAnnotation(new Annotation(key.getCode(), value));
    }

    public void recordApiId(final int apiId) {
        final StackFrame currentStackFrame = this.currentStackFrame;
        currentStackFrame.setApiId(apiId);
    }

    @Override
    public void recordAttribute(final AnnotationKey key, final Object value) {
        final StackFrame currentStackFrame = this.currentStackFrame;
        currentStackFrame.addAnnotation(new Annotation(key.getCode(), value));
    }


    @Override
    public void recordServiceType(final ServiceType serviceType) {
        final StackFrame currentStackFrame = this.currentStackFrame;
        currentStackFrame.setServiceType(serviceType.getCode());
    }

    @Override
    public void recordRpcName(final String rpc) {
        StackFrame currentStackFrame = this.currentStackFrame;
        currentStackFrame.setRpc(rpc);

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
        // TODO Need to unify API                                                                                           
        StackFrame currentStackFrame = this.currentStackFrame;
        currentStackFrame.setEndPoint(endPoint);
    }

    @Override
    public void recordRemoteAddress(final String remoteAddress) {
        // TODO Need to unify API
        StackFrame currentStackFrame = this.currentStackFrame;
        if (currentStackFrame instanceof RootStackFrame) {
            ((RootStackFrame) currentStackFrame).setRemoteAddress(remoteAddress);
        } else {
            throw new PinpointTraceException("not RootStackFrame");
        }
    }

    @Override
    public void recordNextSpanId(long nextSpanId) {
        if (nextSpanId == -1) {
            return;
        }
        StackFrame currentStackFrame = this.currentStackFrame;
        if (currentStackFrame instanceof  SpanEventStackFrame) {
            ((SpanEventStackFrame) currentStackFrame).setNextSpanId(nextSpanId);
        } else {
            throw new PinpointTraceException("not SpanEventStackFrame");
        }
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
    public void recordAsyncId(int asyncId) {
        StackFrame currentStackFrame = this.currentStackFrame;
        if(currentStackFrame instanceof SpanEventStackFrame) {
            ((SpanEventStackFrame) currentStackFrame).setAsyncId(asyncId);
        } else {
            throw new PinpointException("not SpanEventStackFrame");
        }
    }

    @Override
    public void recordNextAsyncId(int asyncId) {
        StackFrame currentStackFrame = this.currentStackFrame;
        if(currentStackFrame instanceof SpanEventStackFrame) {
            ((SpanEventStackFrame) currentStackFrame).setNextAsyncId(asyncId);
        } else {
            throw new PinpointException("not SpanEventStackFrame");
        }
    }

    @Override
    public boolean isAsync() {
        return false;
    }

    @Override
    public long getTraceStartTime() {
        return callStack.getSpan().getStartTime();
    }

    @Override
    public boolean isRootStack() {
        return currentStackFrame != null ? currentStackFrame.getStackFrameId() == ROOT_STACKID : false;
    }

    @Override
    public AsyncTraceId getAsyncTraceId() {
        return new DefaultAsyncTraceId(traceId, traceContext.getAsyncId(), getTraceStartTime());
    }

    @Override
    public void recordAsyncSequence(short asyncSequence) {
        StackFrame currentStackFrame = this.currentStackFrame;
        if(currentStackFrame instanceof SpanEventStackFrame) {
            ((SpanEventStackFrame) currentStackFrame).setAsyncSequence(asyncSequence);
        } else {
            throw new PinpointException("not SpanEventStackFrame");
        }
    }

    @Override
    public void recordLogging(boolean isLogging) {
        final Span span = getCallStack().getSpan();
        
        if (!span.isSetLoggingTransactionInfo()) {
            span.setLoggingTransactionInfo((short)(isLogging ? 1 : 0)); 
        }
    }

    @Override
    public TraceType getTraceType() {
        return this.traceType;
    }
    
    public void setTraceType(TraceType traceType) {
        this.traceType = traceType;
    }
}
