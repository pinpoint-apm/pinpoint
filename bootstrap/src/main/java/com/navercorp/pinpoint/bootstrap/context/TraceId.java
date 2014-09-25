package com.nhn.pinpoint.bootstrap.context;

/**
 * @author emeroad
 */
public interface TraceId {

    TraceId getNextTraceId();

    long getSpanId();

    String getTransactionId();

    String getAgentId();

    long getAgentStartTime();

    long getTransactionSequence();

    long getParentSpanId();

    short getFlags();

    boolean isRoot();
}
