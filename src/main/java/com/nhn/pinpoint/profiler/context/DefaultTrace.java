package com.nhn.pinpoint.profiler.context;

import com.nhn.pinpoint.bootstrap.context.*;
import com.nhn.pinpoint.common.AnnotationKey;
import com.nhn.pinpoint.common.HistogramSchema;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.util.ParsingResult;
import com.nhn.pinpoint.exception.PinpointException;
import com.nhn.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.nhn.pinpoint.bootstrap.util.StringUtils;
import com.nhn.pinpoint.profiler.monitor.metric.Histogram;
import com.nhn.pinpoint.thrift.dto.TIntStringStringValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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

    public DefaultTrace(final TraceContext traceContext, long transactionId) {
        if (traceContext == null) {
            throw new NullPointerException("traceContext must not be null");
        }
        this.traceContext = traceContext;
        this.traceId = new DefaultTraceId(traceContext.getAgentId(), traceContext.getAgentStartTime(), transactionId);

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
        // traceId 레코드를 나중에 해야 된다.
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

    public short getSequence() {
        return sequence++;
    }

    public int getCallStackDepth() {
        return this.callStack.getIndex();
    }


    private StackFrame createSpanEventStackFrame(int stackId) {
        SpanEvent spanEvent = new SpanEvent(callStack.getSpan());
        // Span내부의 SpanEvent로 들어가지 않을 경우 사용하기 위해 set한다.

        SpanEventStackFrame stackFrame = new SpanEventStackFrame(spanEvent);
        stackFrame.setStackFrameId(stackId);
        stackFrame.setSequence(getSequence());
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
        pop(ROOT_STACKID);
        callStack.popRoot();
        // 잘못된 stack 조작시 다음부터 그냥 nullPointerException이 발생할건데 괜찮은가?
        this.currentStackFrame = null;
    }

    @Override
    public void traceBlockEnd() {
        traceBlockEnd(DEFAULT_STACKID);
    }

    private void metricResponseTime() {
        final int errCode = this.getCallStack().getSpan().getErrCode();
        if (errCode != 0) {
            Histogram contextMetric = (Histogram) this.traceContext.getContextMetric();
            contextMetric.addResponseTime(HistogramSchema.ERROR_SLOT_TIME);
        } else {
            final int elapsedTime = this.currentStackFrame.getElapsedTime();
            Histogram contextMetric = (Histogram) this.traceContext.getContextMetric();
            contextMetric.addResponseTime(elapsedTime);
        }
    }



    @Override
    public void traceBlockEnd(int stackId) {
        pop(stackId);
        StackFrame popStackFrame = callStack.pop();
        // pop 할때 frame위치를 원복해야 한다.
        this.currentStackFrame = popStackFrame;
    }

    private void pop(int stackId) {
        final StackFrame currentStackFrame = this.currentStackFrame;
        int stackFrameId = currentStackFrame.getStackFrameId();
        if (stackFrameId != stackId) {
            // 자체 stack dump를 하면 오류발견이 쉬울것으로 생각됨
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
    public void recordException(Object result) {
        if (result instanceof Throwable) {
            final Throwable th = (Throwable) result;
            final String drop = StringUtils.drop(th.getMessage(), 256);
            // exception class가 proxy라서 class Name이 불규칙하면 문제가 발생할수 있다.
            final int exceptionId = traceContext.cacheString(th.getClass().getName());
            this.currentStackFrame.setExceptionInfo(exceptionId, drop);

            final Span span = getCallStack().getSpan();
            if (!span.isSetErrCode()) {
                span.setErrCode(1);
            }
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
        // API 저장 방법의 개선 필요.                                                                                        
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
            // TODO MAX 사이즈를 넘는건 마크만 해줘야 하나?
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
            // TODO MAX 사이즈를 넘는건 마크만 해줘야 하나?                                                                  
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
        final String sql = parsingResult.getSql();
        final TIntStringStringValue tSqlValue = new TIntStringStringValue(sql.hashCode());
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
        // TODO API 단일화 필요.                                                                                             
        StackFrame currentStackFrame = this.currentStackFrame;
        currentStackFrame.setEndPoint(endPoint);
    }

    @Override
    public void recordRemoteAddress(final String remoteAddress) {
        // TODO API 단일화 필요.
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
}                                                                                                                            
