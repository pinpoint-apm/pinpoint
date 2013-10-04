package com.nhn.pinpoint.profiler.context;

/**
 *
 */
public class RootStackFrame implements StackFrame {

    private int stackId;
    private Span span;


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


    public void setSpan(Span span) {
        this.span = span;
    }

    public Span getSpan() {
        return span;
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
