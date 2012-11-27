package com.profiler.context;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.common.ServiceType;
import com.profiler.interceptor.MethodDescriptor;
import com.profiler.sender.DataSender;
import com.profiler.sender.LoggingDataSender;

/**
 * @author netspider
 */
public final class Trace {

    private final Logger logger = Logger.getLogger(Trace.class.getName());

    private static final DataSender DEFULT_DATA_SENDER = new LoggingDataSender();

    public static final int HANDLER_STACKID = -2;
    public static final int NOCHECK_STACKID = -1;
    public static final int ROOT_STACKID = 0;

    private boolean tracingEnabled = true;

    private TraceID root;
    private CallStack callStack;

    private DataSender dataSender = DEFULT_DATA_SENDER;

    public Trace() {
        // traceObject에서 spanid의 유효성을 히스토리를 관리한다면 같은 thread에서는 span랜덤생성아이디의 충돌을 방지할수 있기는 함.
        this.root = TraceID.newTraceId();
        this.callStack = new CallStack();
        this.callStack.push();
        StackFrame stackFrame = createStackFrame(root, ROOT_STACKID);
        this.callStack.setStackFrame(stackFrame);
    }

    public Trace(TraceID continueRoot) {
        this.root = continueRoot;
        this.callStack = new CallStack();
        this.callStack.push();
        StackFrame stackFrame = createStackFrame(continueRoot, ROOT_STACKID);
        this.callStack.setStackFrame(stackFrame);
    }

    public DataSender getDataSender() {
        return dataSender;
    }

    public void setDataSender(DataSender dataSender) {
        this.dataSender = dataSender;
    }

    public void handle(TraceHandler handler) {
        try {
            TraceID nextId = getNextTraceId();
            callStack.push();
            StackFrame stackFrame = createStackFrame(nextId, HANDLER_STACKID);
            callStack.setStackFrame(stackFrame);
            handler.handle(nextId);
        } finally {
            // stackID check하면 좋을듯.
            callStack.pop();
        }
    }

    public AsyncTrace createAsyncTrace() {
        // 경우에 따라 별도 timeout 처리가 있어야 될수도 있음.
        TraceID nextTraceId = getNextTraceId();
        Span span = new Span(nextTraceId);
        AsyncTrace asyncTrace = new AsyncTrace(span);
        asyncTrace.setDataSender(this.getDataSender());
        return asyncTrace;
    }

    private StackFrame createStackFrame(TraceID nextId, int stackId) {
        Span span = new Span(nextId);
        StackFrame stackFrame = new StackFrame(span);
        stackFrame.setStackFrameId(stackId);
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
        TraceID nextId = getNextTraceId();
        callStack.push();
        StackFrame stackFrame = createStackFrame(nextId, stackId);
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
        if (currentStackFrame.getStackFrameId() != stackId) {
            // 자체 stack dump를 하면 오류발견이 쉬울것으로 생각됨.
            logger.warning("Corrupted CallStack found. StackId not matched");
        }
        logSpan(currentStackFrame);
        callStack.pop();
    }

    public StackFrame getCurrentStackFrame() {
        return callStack.getCurrentStackFrame();
    }

//    public boolean removeCurrentTraceIdFromStack() {
//        StackFrame currentStackFrame = callStack.getCurrentStackFrame();
//        if (currentStackFrame != null) {
//            TraceID traceId = currentStackFrame.getTraceID();
//            callStack.currentStackFrameClear();
////            spanMap.remove(traceId);
//            return true;
//        }
//        return false;
//    }

    /**
     * Get current TraceID. If it was not set this will return null.
     *
     * @return
     */
    public TraceID getCurrentTraceId() {
        return callStack.getCurrentStackFrame().getTraceID();
    }

    public void enable() {
        tracingEnabled = true;
    }

    public void disable() {
        tracingEnabled = false;
    }

    public TraceID getNextTraceId() {
        TraceID current = getCurrentTraceId();
        return current.getNextTraceId();
    }


    void logSpan(StackFrame stackFrame) {
        Span span = stackFrame.getSpan();
        try {
            if (logger.isLoggable(Level.INFO)) {
                logger.info("[WRITE SPAN]" + span + " CurrentThreadID=" + Thread.currentThread().getId() + ",\n\t CurrentThreadName=" + Thread.currentThread().getName() + "\n\n");
            }

            // TODO: remove this, just for debugging
            // if (spanMap.size() > 0) {
            // System.out.println("##################################################################");
            // System.out.println("# [DEBUG MSG] WARNING SpanMap size > 0 check spanMap.            #");
            // System.out.println("##################################################################");
            // System.out.println("current spamMap=" + spanMap);
            // }

            dataSender.send(span);
//            span.cancelTimer();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void record(Annotation annotation) {
        if (!tracingEnabled)
            return;

        annotate(annotation.getCode());
    }

    public void record(Annotation annotation, long duration) {
        if (!tracingEnabled)
            return;

        annotate(annotation.getCode());
    }

    public void recordException(Object result) {
        if (result instanceof Throwable) {
            Throwable th = (Throwable) result;
            recordAttribute("Exception", th.getMessage());
        }
    }

    public void recordApi(MethodDescriptor methodDescriptor) {
        if (methodDescriptor == null) {
            return;
        }
        String method = methodDescriptor.getClassName() + "." + methodDescriptor.getMethodName() + methodDescriptor.getSimpleParameterDescriptor() + ":" + methodDescriptor.getLineNumber();
        recordAttribute("API", method);
    }

    public void recordApi(MethodDescriptor methodDescriptor, Object[] args) {
        // API 저장 방법의 개선 필요.
        String method = methodDescriptor.getClassName() + "." + methodDescriptor.getMethodName() + methodDescriptor.getSimpleParameterDescriptor() + ":" + methodDescriptor.getLineNumber();
        recordAttribute("API", method);
        recocordArgs(args);
    }

    private void recocordArgs(Object[] args) {
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                recordAttribute("args[" + i + "]", args[i]);
            }
        }
    }

    public void recordAttribute(final String key, final String value) {
        recordAttribute(key, (Object) value);
    }

    public void recordAttribute(final String key, final Object value) {
        if (!tracingEnabled)
            return;

        try {
            Span span = getCurrentStackFrame().getSpan();
            span.addAnnotation(new HippoAnnotation(System.currentTimeMillis(), key, value));
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void recordMessage(String message) {
        if (!tracingEnabled)
            return;

        annotate(message);
    }

    public void recordRpcName(final ServiceType serviceType, final String serviceName, final String rpc) {
        if (!tracingEnabled)
            return;

        try {
            Span span = getCurrentStackFrame().getSpan();
            span.setServiceType(serviceType);
            span.setServiceName(serviceName);
            span.setRpc(rpc);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void recordTerminalEndPoint(final String endPoint) {
        recordEndPoint(endPoint, true);
    }

    public void recordEndPoint(final String endPoint) {
        recordEndPoint(endPoint, false);
    }

    // TODO: final String... endPoint로 받으면 합치는데 비용이 들어가 그냥 한번에 받는게 나을것 같음.
    private void recordEndPoint(final String endPoint, final boolean isTerminal) {
        if (!tracingEnabled)
            return;

        try {
            Span span = getCurrentStackFrame().getSpan();
            span.setEndPoint(endPoint);
            span.setTerminal(isTerminal);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private void annotate(final String key) {
        if (!tracingEnabled)
            return;

        try {
            Span span = getCurrentStackFrame().getSpan();
            span.addAnnotation(new HippoAnnotation(System.currentTimeMillis(), key));

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }


}