package com.nhn.pinpoint.profiler.logging;

/**
 * @author emeroad
 */
public interface PLoggerBinder {
    PLogger getLogger(String name);

    void shutdown();
}
