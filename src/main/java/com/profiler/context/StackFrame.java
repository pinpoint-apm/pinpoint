package com.profiler.context;

/**
 *
 */
public interface StackFrame {
    int getStackFrameId();

    void setStackFrameId(int stackId);

    void markBeforeTime();

    long getBeforeTime();

    void markAfterTime();

    long getAfterTime();

    void attachObject(Object object);

    Object getAttachObject(Object object);

    Object detachObject();
}
