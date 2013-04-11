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

    boolean isSampled();

    short getFlags();

    boolean isRoot();
}
