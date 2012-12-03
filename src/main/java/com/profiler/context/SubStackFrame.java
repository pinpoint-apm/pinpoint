package com.profiler.context;

/**
 *
 */
public class SubStackFrame implements StackFrame {
    private SubSpan subSpan;
    private int stackId;

    public SubStackFrame(SubSpan subSpan) {
        this.subSpan = subSpan;
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
        subSpan.setStartTime(System.currentTimeMillis());
    }

    @Override
    public long getBeforeTime() {
        return subSpan.getStartTime();
    }

    @Override
    public void markAfterTime() {
        subSpan.setEndTime(System.currentTimeMillis());
    }

    @Override
    public long getAfterTime() {
        return subSpan.getEndTime();
    }

    public void setSequence(short sequence) {
        subSpan.setSequence(sequence);
    }

    public SubSpan getSubSpan() {
        return subSpan;
    }

    @Override
    public void attachObject(Object object) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object getAttachObject(Object object) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Object detachObject() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


}
