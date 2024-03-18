package com.navercorp.pinpoint.profiler.context.id;

/**
 * @author Woonduk Kang(emeroad)
 */
public interface LocalTraceRoot {

    long getLocalTransactionId();

    long getTraceStartTime();

    Shared getShared();
}
