package com.profiler.context;

import com.profiler.common.AnnotationKey;
import com.profiler.common.ServiceType;
import com.profiler.common.util.ParsingResult;
import com.profiler.interceptor.MethodDescriptor;
import com.profiler.logging.Logger;
import com.profiler.logging.LoggerFactory;
import com.profiler.util.StringUtils;

import java.util.List;


/**
 * @author netspider
 */
public final class DefaultTrace implements Trace {

    private static final Logger logger = LoggerFactory.getLogger(DefaultTrace.class.getName());
    private static final boolean isDebug = logger.isDebugEnabled();
    private static final boolean isTrace = logger.isTraceEnabled();

    public static final int NOCHECK_STACKID = -1;
    public static final int ROOT_STACKID = 0;

    private short sequence;

    private boolean tracingEnabled = true;

    private CallStack callStack;

    private Storage storage;

    private TraceContext traceContext;

    // use for calculating depth of each Span.
    private int latestStackIndex = -1;

    public DefaultTrace() {
        DefaultTraceID traceId = DefaultTraceID.newTraceId();
        this.callStack = new CallStack(traceId);
        latestStackIndex = this.callStack.push();
        StackFrame stackFrame = createRootStackFrame(ROOT_STACKID, callStack.getSpan());
        this.callStack.setStackFrame(stackFrame);
    }

    public DefaultTrace(TraceID continueTraceID) {
        // this.root = continueRoot;
        this.callStack = new CallStack(continueTraceID);
        latestStackIndex = this.callStack.push();
        StackFrame stackFrame = createRootStackFrame(ROOT_STACKID, callStack.getSpan());
        this.callStack.setStackFrame(stackFrame);
    }

    public CallStack getCallStack() {
        return callStack;
    }


    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public short getSequence() {
        return sequence++;
    }

    @Override
    public AsyncTrace createAsyncTrace() {
        // 경우에 따라 별도 timeout 처리가 있어야 될수도 있음.
        SpanEvent spanEvent = new SpanEvent(callStack.getSpan());
        spanEvent.setSequence(getSequence());
        DefaultAsyncTrace asyncTrace = new DefaultAsyncTrace(spanEvent);
        // asyncTrace.setDataSender(this.getDataSender());
        asyncTrace.setStorage(this.storage);
        return asyncTrace;
    }

    private StackFrame createSubStackFrame(int stackId) {
        SpanEvent spanEvent = new SpanEvent(callStack.getSpan());
        SubStackFrame stackFrame = new SubStackFrame(spanEvent);
        stackFrame.setStackFrameId(stackId);
        stackFrame.setSequence(getSequence());
        return stackFrame;
    }

    private StackFrame createRootStackFrame(int stackId, Span span) {
        RootStackFrame stackFrame = new RootStackFrame(span);
        stackFrame.setStackFrameId(stackId);
        stackFrame.setSpan(span);
        stackFrame.setStackFrameId(ROOT_STACKID);
        return stackFrame;
    }

    @Override
    public void traceBlockBegin() {
        traceBlockBegin(NOCHECK_STACKID);
    }

    @Override
    public void markBeforeTime() {
        StackFrame stackFrame = getCurrentStackFrame();
        stackFrame.markBeforeTime();
    }

    @Override
    public long getBeforeTime() {
        StackFrame stackFrame = getCurrentStackFrame();
        return stackFrame.getBeforeTime();
    }

    @Override
    public void markAfterTime() {
        StackFrame stackFrame = getCurrentStackFrame();
        stackFrame.markAfterTime();
    }

    @Override
    public long getAfterTime() {
        StackFrame stackFrame = getCurrentStackFrame();
        return stackFrame.getAfterTime();
    }


    @Override
    public void traceBlockBegin(int stackId) {
        int currentStackIndex = callStack.push();
        StackFrame stackFrame = createSubStackFrame(stackId);

        if (latestStackIndex != currentStackIndex) {
            latestStackIndex = currentStackIndex;
            SpanEvent spanEvent = ((SubStackFrame) stackFrame).getSpanEvent();
            spanEvent.setDepth(latestStackIndex);
        }

        callStack.setStackFrame(stackFrame);
    }

    @Override
    public void traceRootBlockEnd() {
        traceBlockEnd(ROOT_STACKID);
    }

    @Override
    public void traceBlockEnd() {
        traceBlockEnd(NOCHECK_STACKID);
    }


    @Override
    public void traceBlockEnd(int stackId) {
        StackFrame currentStackFrame = callStack.getCurrentStackFrame();
        int stackFrameId = currentStackFrame.getStackFrameId();
        if (stackFrameId != stackId) {
            // 자체 stack dump를 하면 오류발견이 쉬울것으로 생각됨
            if (logger.isWarnEnabled()) {
                logger.warn("Corrupted CallStack found. StackId not matched. expected:" + stackId + " current:" + stackFrameId);
            }
        }
        if (currentStackFrame instanceof RootStackFrame) {
            logSpan(((RootStackFrame) currentStackFrame).getSpan());
        } else {
            logSpan(((SubStackFrame) currentStackFrame).getSpanEvent());
        }
        callStack.pop();
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
    public TraceID getTraceId() {
        return callStack.getSpan().getTraceID();
    }

    @Override
    public void enable() {
        tracingEnabled = true;
    }

    @Override
    public void disable() {
        tracingEnabled = false;
    }

    void logSpan(SpanEvent spanEvent) {
        if (isTrace) {
            Thread th = Thread.currentThread();
            logger.trace("[WRITE SpanEvent]" + spanEvent + ", Thread ID=" + th.getId() + " Name=" + th.getName());
        }
        this.storage.store(spanEvent);
    }

    void logSpan(Span span) {
        if (isTrace) {
            Thread th = Thread.currentThread();
            logger.trace("[WRITE SPAN]" + span + ", Thread ID=" + th.getId() + " Name=" + th.getName());
        }
        this.storage.store(span);
    }

    @Override
    public void recordException(Object result) {
        if (result instanceof Throwable) {
            Throwable th = (Throwable) result;
            String drop = StringUtils.drop(th.getMessage(), 256);
            recordAttribute(AnnotationKey.EXCEPTION, drop);

            Span span = getCallStack().getSpan();
            if (span.getException() == 0) {
                span.setException(1);
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
            recordAttribute(AnnotationKey.API_DID, methodDescriptor.getApiId());
        }
    }

    @Override
    public void recordApi(MethodDescriptor methodDescriptor, Object[] args) {
        // API 저장 방법의 개선 필요.
        recordApi(methodDescriptor);
        recocordArgs(args);
    }

    @Override
    public void recordApi(int apiId) {
        recordAttribute(AnnotationKey.API_ID, apiId);
    }

    @Override
    public void recordApi(int apiId, Object[] args) {
        recordAttribute(AnnotationKey.API_ID, apiId);
        recocordArgs(args);
    }

    private void recocordArgs(Object[] args) {
        if (args != null) {
            int min = Math.min(args.length, AnnotationKey.MAX_ARGS_SIZE);
            for (int i = 0; i < min; i++) {
                recordAttribute(AnnotationKey.getArgs(i), args[i]);
            }
            // TODO MAX 사이즈를 넘는건 마크만 해줘야 하나?
        }
    }


    @Override
    public void recordAttribute(final AnnotationKey key, final String value) {
        recordAttribute(key, (Object) value);
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
        recordAttribute(AnnotationKey.SQL_ID, parsingResult.getSql().hashCode());
        String output = parsingResult.getOutput();
        if (output != null && output.length() != 0) {
            recordAttribute(AnnotationKey.SQL_PARAM, output);
        }
    }

    @Override
    public void recordAttribute(final AnnotationKey key, final Object value) {
        // TODO API 단일화 필요.
        StackFrame currentStackFrame = getCurrentStackFrame();
        if (currentStackFrame instanceof RootStackFrame) {
            Span span = ((RootStackFrame) currentStackFrame).getSpan();
            span.addAnnotation(new TraceAnnotation(key, value));
        } else {
            SpanEvent spanEvent = ((SubStackFrame) currentStackFrame).getSpanEvent();
            spanEvent.addAnnotation(new TraceAnnotation(key, value));
        }

    }


    @Override
    public void recordServiceType(final ServiceType serviceType) {
        // TODO API 단일화 필요.
        StackFrame currentStackFrame = getCurrentStackFrame();
        if (currentStackFrame instanceof RootStackFrame) {
            Span span = ((RootStackFrame) currentStackFrame).getSpan();
            span.setServiceType(serviceType);
        } else {
            SpanEvent spanEvent = ((SubStackFrame) currentStackFrame).getSpanEvent();
            spanEvent.setServiceType(serviceType);
        }

    }

    @Override
    public void recordRpcName(final String rpc) {
        // TODO API 단일화 필요.
        StackFrame currentStackFrame = getCurrentStackFrame();
        if (currentStackFrame instanceof RootStackFrame) {
            Span span = ((RootStackFrame) currentStackFrame).getSpan();
            span.setRpc(rpc);
        } else {
            SpanEvent spanEvent = ((SubStackFrame) currentStackFrame).getSpanEvent();
            spanEvent.setRpc(rpc);
        }

    }

    @Override
    public void recordDestinationId(final String destinationId) {
        // TODO API 단일화 필요.
        StackFrame currentStackFrame = getCurrentStackFrame();
        if (currentStackFrame instanceof SubStackFrame) {
            SpanEvent spanEvent = ((SubStackFrame) currentStackFrame).getSpanEvent();
            spanEvent.setDestionationId(destinationId);
        }
    }

    @Override
    public void recordDestinationAddress(List<String> address) {
        // TODO API 단일화 필요.
        StackFrame currentStackFrame = getCurrentStackFrame();
        if (currentStackFrame instanceof SubStackFrame) {
            SpanEvent spanEvent = ((SubStackFrame) currentStackFrame).getSpanEvent();
            spanEvent.setDestinationAddress();
        }
    }

    @Override
    public void recordDestinationAddressList(List<String> addressList) {
        //To change body of created methods use File | Settings | File Templates.
    }

    @Override
    public void recordEndPoint(final String endPoint) {
        // TODO API 단일화 필요.
        StackFrame currentStackFrame = getCurrentStackFrame();
        if (currentStackFrame instanceof RootStackFrame) {
            Span span = ((RootStackFrame) currentStackFrame).getSpan();
            span.setEndPoint(endPoint);
        } else {
            SpanEvent spanEvent = ((SubStackFrame) currentStackFrame).getSpanEvent();
            spanEvent.setEndPoint(endPoint);
        }
    }
    
    @Override
    public void recordRemoteAddr(final String remoteAddr) {
    	// TODO API 단일화 필요.
    	StackFrame currentStackFrame = getCurrentStackFrame();
    	if (currentStackFrame instanceof RootStackFrame) {
    		Span span = ((RootStackFrame) currentStackFrame).getSpan();
    		span.setRemoteAddr(remoteAddr);
    	} else {
    		// do nothing.
    	}
    }

    @Override
    public void recordNextSpanId(int spanId) {
        StackFrame currentStackFrame = getCurrentStackFrame();
        if (currentStackFrame instanceof RootStackFrame) {
            logger.warn("OMG. Something's going wrong. Current stackframe is root Span. nextSpanId={}", spanId);
        } else {
            SpanEvent spanEvent = ((SubStackFrame) currentStackFrame).getSpanEvent();
            spanEvent.setNextSpanId(spanId);
        }
    }

    private void annotate(final AnnotationKey key) {
        StackFrame currentStackFrame = getCurrentStackFrame();
        if (currentStackFrame instanceof RootStackFrame) {
            Span span = ((RootStackFrame) currentStackFrame).getSpan();
            span.addAnnotation(new TraceAnnotation(key));
        } else {
            SpanEvent spanEvent = ((SubStackFrame) currentStackFrame).getSpanEvent();
            spanEvent.addAnnotation(new TraceAnnotation(key));
        }

    }

    @Override
    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

    @Override
    public void recordParentApplication(String parentApplicationName, short parentApplicationType) {
    	StackFrame currentStackFrame = getCurrentStackFrame();
    	if (currentStackFrame instanceof RootStackFrame) {
    		Span span = ((RootStackFrame) currentStackFrame).getSpan();
    		span.setParentApplicationName(parentApplicationName);
    		span.setParentApplicationType(parentApplicationType);
            if (isDebug) {
			    logger.debug("ParentApplicationName marked. parentApplicationName={}", parentApplicationName);
            }
    	} else {
    		// do nothing.
    	}
    }
    
    @Override
    public void recordAcceptorHost(String host) {
    	StackFrame currentStackFrame = getCurrentStackFrame();
    	if (currentStackFrame instanceof RootStackFrame) {
    		Span span = ((RootStackFrame) currentStackFrame).getSpan();
    		span.setAcceptorHost(host); // me
            if (isDebug) {
    		    logger.debug("Acceptor host received. host={}", host);
            }
    	} else {
    		// do nothing.
    	}
    }

    @Override
    public int getStackFrameId() {
        return this.getCurrentStackFrame().getStackFrameId();

    }
}