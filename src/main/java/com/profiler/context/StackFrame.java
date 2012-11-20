package com.profiler.context;

/**
 *
 */
public class StackFrame {

    private int stackId;
    private Span span;

    public StackFrame(Span span) {
        this.span = span;
    }


    public TraceID getTraceID() {
        return span.getTraceID();
    }

    public int getStackFrameId() {
        return stackId;
    }

    public void setStackFrameId(int stackId) {
        this.stackId = stackId;
    }

    public void markBeforeTime() {
        this.span.setStartTime(System.currentTimeMillis());
    }

    public long getBeforeTime() {
        return this.span.getStartTime();
    }

    public void markAfterTime() {
        this.span.setEndTime(System.currentTimeMillis());
    }

    public long getAfterTime() {
        return this.span.getEndTime();
    }


    public void setSpan(Span span) {
        this.span = span;
    }

    public Span getSpan() {
        return span;
    }

    public void attachObject(Object object) {
    }

    public Object getAttachObject(Object object) {
        return null;
    }

    public Object detachObject() {
        return null;
    }
}
