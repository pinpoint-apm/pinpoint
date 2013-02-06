package com.profiler.context;

import com.profiler.common.AnnotationNames;
import com.profiler.common.ServiceType;
import com.profiler.common.util.ParsingResult;
import com.profiler.interceptor.MethodDescriptor;
import com.profiler.logging.LoggingUtils;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author netspider
 */
public final class Trace {

    private static final Logger logger = Logger.getLogger(Trace.class.getName());
    private static final boolean isDebug = LoggingUtils.isDebug(logger);

    public static final int NOCHECK_STACKID = -1;
    public static final int ROOT_STACKID = 0;

    private short sequence;

    private boolean tracingEnabled = true;

    private CallStack callStack;

    private Storage storage;

    private TraceContext traceContext;

    // use for calculating depth of each Span.
    private int latestStackIndex = -1;

    public Trace() {
        // traceObject에서 spanid의 유효성을 히스토리를 관리한다면 같은 thread에서는 span랜덤생성아이디의 충돌을 방지할수 있기는 함.
        TraceID traceId = TraceID.newTraceId();
        this.callStack = new CallStack(traceId);
        latestStackIndex = this.callStack.push();
        StackFrame stackFrame = createRootStackFrame(ROOT_STACKID, callStack.getSpan());
        this.callStack.setStackFrame(stackFrame);
    }

    public Trace(TraceID continueRoot) {
        // this.root = continueRoot;
        this.callStack = new CallStack(continueRoot);
        latestStackIndex = this.callStack.push();
        // StackFrame stackFrame = createStackFrame(ROOT_STACKID);
        StackFrame stackFrame = createRootStackFrame(ROOT_STACKID, callStack.getSpan());
        this.callStack.setStackFrame(stackFrame);
    }

    public CallStack getCallStack() {
        return callStack;
    }


    public Storage getStorage() {
        return storage;
    }

    public void setStorage(Storage storage) {
        this.storage = storage;
    }

    public short getSequence() {
        return sequence++;
    }

    public AsyncTrace createAsyncTrace() {
        // 경우에 따라 별도 timeout 처리가 있어야 될수도 있음.
        SubSpan subSpan = new SubSpan(callStack.getSpan());
        subSpan.setSequence(getSequence());
        AsyncTrace asyncTrace = new AsyncTrace(subSpan);
        // asyncTrace.setDataSender(this.getDataSender());
        asyncTrace.setStorage(this.storage);
        return asyncTrace;
    }

    private StackFrame createSubStackFrame(int stackId) {
        SubSpan subSpan = new SubSpan(callStack.getSpan());
        SubStackFrame stackFrame = new SubStackFrame(subSpan);
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

    public void traceBlockBegin() {
        traceBlockBegin(NOCHECK_STACKID);
    }

    public void markBeforeTime() {
        StackFrame stackFrame = getCurrentStackFrame();
        stackFrame.markBeforeTime();
    }

    public long getBeforeTime() {
        StackFrame stackFrame = getCurrentStackFrame();
        return stackFrame.getBeforeTime();
    }

    public void markAfterTime() {
        StackFrame stackFrame = getCurrentStackFrame();
        stackFrame.markAfterTime();
    }

    public long getAfterTime() {
        StackFrame stackFrame = getCurrentStackFrame();
        return stackFrame.getAfterTime();
    }

//    public void attachObject(Object object) {
//        StackFrame stackFrame = getCurrentStackFrame();
//        stackFrame.attachObject(object);
//    }

    public void traceBlockBegin(int stackId) {
        int currentStackIndex = callStack.push();
        StackFrame stackFrame = createSubStackFrame(stackId);

        if (latestStackIndex != currentStackIndex) {
            latestStackIndex = currentStackIndex;
            SubSpan span = ((SubStackFrame) stackFrame).getSubSpan();
            span.setDepth(latestStackIndex);
        }

        callStack.setStackFrame(stackFrame);
    }

    public void traceBlockEnd() {
        traceBlockEnd(NOCHECK_STACKID);
    }

//    public void traceBlockFinalEnd() {
//        StackFrame currentStackFrame = callStack.getCurrentStackFrame();
//        if (currentStackFrame.getStackFrameId() != ROOT_STACKID) {
//            // 자체 stack dump를 하면 오류발견이 쉬울것으로 생각됨.
//            logger.warning("Corrupted RootCallStack found. StackId not matched");
//        }
//        logSpan(currentStackFrame);
//    }

    public void traceBlockEnd(int stackId) {
        StackFrame currentStackFrame = callStack.getCurrentStackFrame();
        int stackFrameId = currentStackFrame.getStackFrameId();
        if (stackFrameId != stackId) {
            // 자체 stack dump를 하면 오류발견이 쉬울것으로 생각됨
            if (logger.isLoggable(Level.WARNING)) {
                logger.warning("Corrupted CallStack found. StackId not matched. expected:" + stackId + " current:" + stackFrameId);
            }
        }
        if (currentStackFrame instanceof RootStackFrame) {
            logSpan(((RootStackFrame) currentStackFrame).getSpan());
        } else {
            logSpan(((SubStackFrame) currentStackFrame).getSubSpan());
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
    public TraceID getTraceId() {
        return callStack.getSpan().getTraceID();
    }

    public void enable() {
        tracingEnabled = true;
    }

    public void disable() {
        tracingEnabled = false;
    }

    void logSpan(SubSpan subSpan) {
        try {
            if (isDebug) {
                logger.fine("[WRITE SubSPAN]" + subSpan + " CurrentThreadID=" + Thread.currentThread().getId() + ",\n\t CurrentThreadName=" + Thread.currentThread().getName() + "\n\n");
            }
            this.storage.store(subSpan);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    void logSpan(Span span) {
        try {
            if (isDebug) {
                logger.info("[WRITE SPAN]" + span + " CurrentThreadID=" + Thread.currentThread().getId() + ",\n\t CurrentThreadName=" + Thread.currentThread().getName() + "\n\n");
            }
            this.storage.store(span);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void recordException(Object result) {
        if (result instanceof Throwable) {
            Throwable th = (Throwable) result;
            recordAttribute(AnnotationNames.EXCEPTION, th.getMessage());

            try {
                StackFrame currentStackFrame = getCurrentStackFrame();
                if (currentStackFrame instanceof RootStackFrame) {
                    ((RootStackFrame) currentStackFrame).getSpan().setException(true);
                } else {
                    ((SubStackFrame) currentStackFrame).getSubSpan().setException(true);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, e.getMessage(), e);
            }
        }
    }

    public void recordApi(MethodDescriptor methodDescriptor) {
        if (methodDescriptor == null) {
            return;
        }
        if (methodDescriptor.getApiId() == -1) {
            recordAttribute(AnnotationNames.API, methodDescriptor.getFullName());
        } else {
            recordAttribute(AnnotationNames.API_DID, methodDescriptor.getApiId());
        }
    }

    public void recordApi(MethodDescriptor methodDescriptor, Object[] args) {
        // API 저장 방법의 개선 필요.
        recordApi(methodDescriptor);
        recocordArgs(args);
    }

    public void recordApi(int apiId) {
        recordAttribute(AnnotationNames.API_ID, apiId);
    }

    public void recordApi(int apiId, Object[] args) {
        recordAttribute(AnnotationNames.API_ID, apiId);
        recocordArgs(args);
    }

    private void recocordArgs(Object[] args) {
        if (args != null) {
            int min = Math.min(args.length, AnnotationNames.MAX_ARGS_SIZE);
            for (int i = 0; i < min; i++) {
                recordAttribute(AnnotationNames.getArgs(i), args[i]);
            }
            // TODO MAX 사이즈를 넘는건 마크만 해줘야 하나?
        }
    }


    public void recordAttribute(final AnnotationNames key, final String value) {
        recordAttribute(key, (Object) value);
    }

    public ParsingResult recordSqlInfo(String sql) {
        if (sql == null) {
            return null;
        }
        ParsingResult parsingResult = traceContext.parseSql(sql);
        recordSqlParsingResult(parsingResult);
        return parsingResult;
    }

    public void recordSqlParsingResult(ParsingResult parsingResult) {
        recordAttribute(AnnotationNames.SQL_ID, parsingResult.getSql().hashCode());
        String output = parsingResult.getOutput();
        if (output != null && output.length() != 0) {
            recordAttribute(AnnotationNames.SQL_PARAM, output);
        }
    }

    public void recordAttribute(final AnnotationNames key, final Object value) {
        if (!tracingEnabled)
            return;

        try {
            // TODO API 단일화 필요.
            StackFrame currentStackFrame = getCurrentStackFrame();
            if (currentStackFrame instanceof RootStackFrame) {
                Span span = ((RootStackFrame) currentStackFrame).getSpan();
                span.addAnnotation(new Annotation(key, value));
            } else {
                SubSpan span = ((SubStackFrame) currentStackFrame).getSubSpan();
                span.addAnnotation(new Annotation(key, value));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }


    public void recordRpcName(final ServiceType serviceType, final String serviceName, final String rpc) {
        if (!tracingEnabled)
            return;

        try {
            // TODO API 단일화 필요.
            StackFrame currentStackFrame = getCurrentStackFrame();
            if (currentStackFrame instanceof RootStackFrame) {
                Span span = ((RootStackFrame) currentStackFrame).getSpan();
                span.setServiceType(serviceType);
                span.setServiceName(serviceName);
                span.setRpc(rpc);
            } else {
                SubSpan span = ((SubStackFrame) currentStackFrame).getSubSpan();
                span.setServiceType(serviceType);
                span.setServiceName(serviceName);
                span.setRpc(rpc);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void recordEndPoint(final String endPoint) {
        if (!tracingEnabled)
            return;
        // TODO API 단일화 필요.
        try {
            StackFrame currentStackFrame = getCurrentStackFrame();
            if (currentStackFrame instanceof RootStackFrame) {
                Span span = ((RootStackFrame) currentStackFrame).getSpan();
                span.setEndPoint(endPoint);
            } else {
                SubSpan span = ((SubStackFrame) currentStackFrame).getSubSpan();
                span.setEndPoint(endPoint);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void recordNextSpanId(int spanId) {
        if (!tracingEnabled)
            return;

        try {
            StackFrame currentStackFrame = getCurrentStackFrame();
            if (currentStackFrame instanceof RootStackFrame) {
                logger.log(Level.WARNING, "OMG. Something's going wrong. Current stackframe is root Span. nextSpanId={}", spanId);
            } else {
                SubSpan span = ((SubStackFrame) currentStackFrame).getSubSpan();
                span.setNextSpanId(spanId);
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void annotate(final AnnotationNames key) {
        if (!tracingEnabled)
            return;

        try {
            StackFrame currentStackFrame = getCurrentStackFrame();
            if (currentStackFrame instanceof RootStackFrame) {
                Span span = ((RootStackFrame) currentStackFrame).getSpan();
                span.addAnnotation(new Annotation(key));
            } else {
                SubSpan span = ((SubStackFrame) currentStackFrame).getSubSpan();
                span.addAnnotation(new Annotation(key));
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void setTraceContext(TraceContext traceContext) {
        this.traceContext = traceContext;
    }

}