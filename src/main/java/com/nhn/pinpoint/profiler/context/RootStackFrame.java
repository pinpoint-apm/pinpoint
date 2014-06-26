package com.nhn.pinpoint.profiler.context;

/**
 * @author emeroad
 */
public class RootStackFrame implements StackFrame {

    private final Span span;
    private int stackId;


    public RootStackFrame(Span span) {
        if (span == null) {
            throw new NullPointerException("span must not be null");
        }
        this.span = span;
    }


    @Override
    public int getStackFrameId() {
        return stackId;
    }

    @Override
    public void setStackFrameId(int stackId) {
        this.stackId = stackId;
    }

    @Override
    public void markBeforeTime() {
        this.span.markBeforeTime();
    }

    @Override
    public long getBeforeTime() {
        return this.span.getStartTime();
    }

    @Override
    public void markAfterTime() {
        this.span.markAfterTime();
    }

    @Override
    public long getAfterTime() {
        return span.getAfterTime();
    }

    @Override
    public int getElapsedTime() {
        return span.getElapsed();
    }


    public Span getSpan() {
        return span;
    }

    @Override
    public void setEndPoint(String endPoint) {
        this.span.setEndPoint(endPoint);
    }

    @Override
    public void setRpc(String rpc) {
        this.span.setRpc(rpc);
    }

    @Override
    public void setApiId(int apiId) {
        this.span.setApiId(apiId);
    }

    @Override
    public void setExceptionInfo(int exceptionId, String exceptionMessage) {
        this.span.setExceptionInfo(exceptionId, exceptionMessage);
    }

    @Override
    public void setServiceType(short serviceType) {
        this.span.setServiceType(serviceType);
    }

    @Override
    public void addAnnotation(Annotation annotation) {
        this.span.addAnnotation(annotation);
    }

    public void setRemoteAddress(String remoteAddress) {
        this.span.setRemoteAddr(remoteAddress);
    }

    @Override
    public void attachObject(Object object) {
    }

    @Override
    public Object getAttachObject(Object object) {
        return null;
    }

    @Override
    public Object detachObject() {
        return null;
    }
}
