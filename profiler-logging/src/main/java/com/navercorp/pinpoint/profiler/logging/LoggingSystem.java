package com.navercorp.pinpoint.profiler.logging;

import java.util.logging.Handler;

public interface LoggingSystem {
    void start();

    Handler getJulHandler();

    void stop();
}
