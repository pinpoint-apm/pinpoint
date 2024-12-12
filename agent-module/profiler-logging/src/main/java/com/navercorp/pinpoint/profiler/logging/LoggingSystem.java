package com.navercorp.pinpoint.profiler.logging;

import java.io.Closeable;

public interface LoggingSystem extends Closeable {
    String getConfigLocation();

    void start();

    void close();
}
