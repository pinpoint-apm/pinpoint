package com.profiler.context;

import java.util.UUID;

/**
 *
 */
public interface TraceID {

    TraceID getNextTraceId();

    int getSpanId();

    UUID getId();

    int getParentSpanId();

    short getFlags();

    boolean isRoot();
}
