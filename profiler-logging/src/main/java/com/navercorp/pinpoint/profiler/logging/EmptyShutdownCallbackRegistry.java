package com.navercorp.pinpoint.profiler.logging;

import org.apache.logging.log4j.core.util.Cancellable;
import org.apache.logging.log4j.core.util.ShutdownCallbackRegistry;

public class EmptyShutdownCallbackRegistry implements ShutdownCallbackRegistry {

    @Override
    public Cancellable addShutdownCallback(Runnable callback) {
        return null;
    }
}
