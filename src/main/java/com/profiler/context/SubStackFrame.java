package com.profiler.context;

/**
 *
 */
public class SubStackFrame implements StackFrame {
    private SpanEvent spanEvent;
    private int stackId;

    public SubStackFrame(SpanEvent spanEvent) {
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
        spanEvent.setStartTime(System.currentTimeMillis());
    }

    @Override
    public long getBeforeTime() {
        return spanEvent.getStartTime();
    }

    @Override
    public void markAfterTime() {
        spanEvent.setEndTime(System.currentTimeMillis());
    }

    @Override
    public long getAfterTime() {
        return spanEvent.getEndTime();
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
