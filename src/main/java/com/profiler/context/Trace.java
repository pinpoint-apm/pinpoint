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

    private short sequence;

    private boolean tracingEnabled = true;

    private CallStack callStack;

    private Storage storage;

    public Trace() {
        // traceObject에서 spanid의 유효성을 히스토리를 관리한다면 같은 thread에서는 span랜덤생성아이디의 충돌을 방지할수 있기는 함.
        TraceID traceId = TraceID.newTraceId();
        this.callStack = new CallStack(traceId);
        this.callStack.push();
        StackFrame stackFrame = createRootStackFrame(ROOT_STACKID, callStack.getSpan());
        this.callStack.setStackFrame(stackFrame);
    }

    public Trace(TraceID continueRoot) {
//        this.root = continueRoot;
        this.callStack = new CallStack(continueRoot);
        this.callStack.push();
//        StackFrame stackFrame = createStackFrame(ROOT_STACKID);
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

    public DataSender getDataSender() {
        return storage.getDataSender();
    }


    public AsyncTrace createAsyncTrace() {
        // 경우에 따라 별도 timeout 처리가 있어야 될수도 있음.
        SubSpan subSpan = new SubSpan(callStack.getSpan());
        subSpan.setSequence(sequence++);
        AsyncTrace asyncTrace = new AsyncTrace(subSpan);
        asyncTrace.setDataSender(this.getDataSender());
        return asyncTrace;
    }

    private StackFrame createSubStackFrame(int stackId) {
        SubSpan subSpan = new SubSpan(callStack.getSpan());
        SubStackFrame stackFrame = new SubStackFrame(subSpan);
        stackFrame.setStackFrameId(stackId);
        stackFrame.setSequence(sequence++);
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
//        TraceID nextId = getNextTraceId();
        callStack.push();
        StackFrame stackFrame = createSubStackFrame(stackId);
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
            if (logger.isLoggable(Level.INFO)) {
                logger.info("[WRITE SubSPAN]" + subSpan + " CurrentThreadID=" + Thread.currentThread().getId() + ",\n\t CurrentThreadName=" + Thread.currentThread().getName() + "\n\n");
            }
//            if (flushType == 0) {
//                storage.store(subSpan);
//            } else if(flushType == 1) {
//                dataSender.send(subSpan);
            this.storage.store(subSpan);
//            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    void logSpan(Span span) {
        try {
            if (logger.isLoggable(Level.INFO)) {
                logger.info("[WRITE SPAN]" + span + " CurrentThreadID=" + Thread.currentThread().getId() + ",\n\t CurrentThreadName=" + Thread.currentThread().getName() + "\n\n");
            }

//            dataSender.send(span);
            this.storage.store(span);
//            subSpan.cancelTimer();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void record(Annotation annotation) {
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
        String method = methodDescriptor.getClassName() + "." + methodDescriptor.getMethodName() + methodDescriptor.getParameterDescriptor() + ":" + methodDescriptor.getLineNumber();
        recordAttribute("API", method);
    }

    public void recordApi(MethodDescriptor methodDescriptor, Object[] args) {
        // API 저장 방법의 개선 필요.
        String method = methodDescriptor.getClassName() + "." + methodDescriptor.getMethodName() + methodDescriptor.getParameterDescriptor() + ":" + methodDescriptor.getLineNumber();
        recordAttribute("API", method);
        recocordArgs(args);
    }

    public void recordApi(int apiId) {
        recordAttribute("API-ID", apiId);
    }

    public void recordApi(int apiId, Object[] args) {
        recordAttribute("API-ID", apiId);
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
            // TODO API 단일화 필요.
            StackFrame currentStackFrame = getCurrentStackFrame();
            if (currentStackFrame instanceof RootStackFrame) {
                Span span = ((RootStackFrame) currentStackFrame).getSpan();
                span.addAnnotation(new HippoAnnotation(System.currentTimeMillis(), key, value));
            } else {
                SubSpan span = ((SubStackFrame) currentStackFrame).getSubSpan();
                span.addAnnotation(new HippoAnnotation(System.currentTimeMillis(), key, value));
            }
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

    // TODO: final String... endPoint로 받으면 합치는데 비용이 들어가 그냥 한번에 받는게 나을것 같음.
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

    private void annotate(final String key) {
        if (!tracingEnabled)
            return;

        try {
            StackFrame currentStackFrame = getCurrentStackFrame();
            if (currentStackFrame instanceof RootStackFrame) {
                Span span = ((RootStackFrame) currentStackFrame).getSpan();
                span.addAnnotation(new HippoAnnotation(System.currentTimeMillis(), key));
            } else {
                SubSpan span = ((SubStackFrame) currentStackFrame).getSubSpan();
                span.addAnnotation(new HippoAnnotation(System.currentTimeMillis(), key));
            }

        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }
    }


}