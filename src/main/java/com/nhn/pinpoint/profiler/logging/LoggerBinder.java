package com.nhn.pinpoint.profiler.logging;

/**
 *
 */
public interface LoggerBinder {
    Logger getLogger(String name);

    void shutdown();
}
