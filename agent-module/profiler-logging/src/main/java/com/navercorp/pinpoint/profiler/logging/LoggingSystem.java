package com.navercorp.pinpoint.profiler.logging;

import java.io.Closeable;

public interface LoggingSystem extends Closeable {
    void start();

    void close();
}
