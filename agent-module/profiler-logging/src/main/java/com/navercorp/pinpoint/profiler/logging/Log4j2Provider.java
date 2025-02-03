package com.navercorp.pinpoint.profiler.logging;

import org.apache.logging.log4j.spi.NoOpThreadContextMap;
import org.apache.logging.log4j.spi.Provider;

public class Log4j2Provider extends Provider {
    public Log4j2Provider() {
        super(11, CURRENT_VERSION, Log4j2ContextFactory.class, NoOpThreadContextMap.class);
    }
}
