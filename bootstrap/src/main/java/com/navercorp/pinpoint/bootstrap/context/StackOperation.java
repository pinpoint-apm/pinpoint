package com.nhn.pinpoint.bootstrap.context;

/**
 * @author emeroad
 */
public interface StackOperation {

    public static final int DEFAULT_STACKID = -1;
    public static final int ROOT_STACKID = 0;

    void traceBlockBegin();

    void traceBlockBegin(int stackId);

    // traceRootBlockBegin을 명시적으로 빼내야 되듯함.

    void traceRootBlockEnd();

    void traceBlockEnd();

    void traceBlockEnd(int stackId);
}
