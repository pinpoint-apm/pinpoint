package com.navercorp.pinpoint.profiler;

import java.io.Closeable;

/**
 * For Test
 */
public interface Agent extends Closeable {

    void start();

    @Override
    void close();

    void registerStopHandler();
}

