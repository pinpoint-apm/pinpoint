package com.nhn.pinpoint.profiler.context;

import java.util.UUID;

/**
 *
 */
public interface TraceID {

    TraceID getNextTraceId();

    int getSpanId();

    String getId();

    String getAgentId();

    long getAgentStartTime();

    long getTransactionId();

    int getParentSpanId();

    short getFlags();

    boolean isRoot();
}
