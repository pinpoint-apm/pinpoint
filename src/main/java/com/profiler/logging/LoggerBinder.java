package com.profiler.logging;

/**
 *
 */
public interface LoggerBinder {
    Logger getLogger(String name);

    void shutdown();
}
