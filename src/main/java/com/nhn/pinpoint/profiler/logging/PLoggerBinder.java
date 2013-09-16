package com.nhn.pinpoint.profiler.logging;

/**
 *
 */
public interface PLoggerBinder {
    PLogger getLogger(String name);

    void shutdown();
}
