package com.nhn.pinpoint.profiler.context;

/**
 *
 */
public class SpanEventStackFrame implements StackFrame {
    private SpanEvent spanEvent;
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

    public void setSequence(short sequence) {
        spanEvent.setSequence(sequence);
    }

    public SpanEvent getSpanEvent() {
        return spanEvent;
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
