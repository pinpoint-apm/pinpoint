package com.nhn.pinpoint.profiler.context;

/**
 *
 */
public interface TraceID {

    TraceID getNextTraceId();

    int getSpanId();

    String getTransactionId();

    String getAgentId();

    long getAgentStartTime();

    long getTransactionSequence();

    int getParentSpanId();

    short getFlags();

    boolean isRoot();
}
