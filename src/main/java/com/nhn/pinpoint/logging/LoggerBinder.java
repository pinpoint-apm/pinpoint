package com.nhn.pinpoint.logging;

/**
 *
 */
public interface LoggerBinder {
    Logger getLogger(String name);

    void shutdown();
}
