package com.profiler.context;

/**
 *
 */
public class StackFrame {
    private TraceID traceID;
    private int stackId;
    private long time;
    private Span span;

    public StackFrame() {
    }


    public TraceID getTraceID() {
        return traceID;
    }

    public void setTraceID(TraceID traceID) {
        this.traceID = traceID;
    }

    public int getStackFrameId() {
        return stackId;
    }

    public void setStackFrameId(int stackId) {
        this.stackId = stackId;
    }

    public void markBeforeTime() {
        this.time = System.currentTimeMillis();
    }

    public long afterTime() {
        return System.currentTimeMillis() - this.time;
    }

    public long getTime() {
        return this.time;
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
