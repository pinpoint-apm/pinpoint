package com.nhn.pinpoint.profiler.context;

/**
 * @author emeroad
 */
public class SpanEventStackFrame implements StackFrame {
    private final SpanEvent spanEvent;
    private int stackId;

    public SpanEventStackFrame(SpanEvent spanEvent) {
        if (spanEvent == null) {
            throw new NullPointerException("spanEvent must not be null");
        }
        this.spanEvent = spanEvent;
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
        spanEvent.markStartTime();
    }

    @Override
    public long getBeforeTime() {
        return spanEvent.getStartTime();
    }

    @Override
    public void markAfterTime() {
        spanEvent.markAfterTime();
    }

    @Override
    public long getAfterTime() {
        return spanEvent.getAfterTime();
    }

    @Override
    public int getElapsedTime() {
        return spanEvent.getEndElapsed();
    }

    public void setSequence(short sequence) {
        spanEvent.setSequence(sequence);
    }

    public SpanEvent getSpanEvent() {
        return spanEvent;
    }

    @Override
    public void setEndPoint(String endPoint) {
        this.spanEvent.setEndPoint(endPoint);
    }

    @Override
    public void setRpc(String rpc) {
        this.spanEvent.setRpc(rpc);
    }

    @Override
    public void setApiId(int apiId) {
        this.spanEvent.setApiId(apiId);
    }

    @Override
    public void setExceptionInfo(int exceptionId, String exceptionMessage) {
        this.spanEvent.setExceptionInfo(exceptionId, exceptionMessage);
    }

    @Override
    public void setServiceType(short serviceType) {
        spanEvent.setServiceType(serviceType);
    }

    @Override
    public void addAnnotation(Annotation annotation) {
        this.spanEvent.addAnnotation(annotation);
    }

    public void setDestinationId(String destinationId) {
        this.spanEvent.setDestinationId(destinationId);
    }

    public void setNextSpanId(long nextSpanId) {
        this.spanEvent.setNextSpanId(nextSpanId);
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
