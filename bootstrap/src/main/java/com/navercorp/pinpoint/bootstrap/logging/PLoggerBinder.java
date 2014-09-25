package com.nhn.pinpoint.bootstrap.logging;

/**
 * @author emeroad
 */
public interface PLoggerBinder {
    PLogger getLogger(String name);

    void shutdown();
}
