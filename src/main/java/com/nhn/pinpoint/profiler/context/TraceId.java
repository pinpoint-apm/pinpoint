package com.nhn.pinpoint.profiler.context;

/**
 *
 */
public interface TraceId {

    TraceId getNextTraceId();

    int getSpanId();

    String getTransactionId();

    String getAgentId();

    long getAgentStartTime();

    long getTransactionSequence();

    int getParentSpanId();

    short getFlags();

    boolean isRoot();
}
