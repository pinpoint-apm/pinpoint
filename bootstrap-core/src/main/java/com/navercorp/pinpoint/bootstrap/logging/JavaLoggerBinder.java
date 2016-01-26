package com.navercorp.pinpoint.bootstrap.logging;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class JavaLoggerBinder {
    
    public final static Handler[] handlers = LogManager.getLogManager().getLogger("").getHandlers();

    private ConcurrentMap<String, Logger> loggerCache = new ConcurrentHashMap<String, Logger>(256, 0.75f, 128);

    public Logger getLogger(String name) {

        final Logger hitLogger = loggerCache.get(name);
        if (hitLogger != null) {
            return hitLogger;
        }

        Logger logger = Logger.getLogger(name);
        
        for (Handler handler : handlers) {
            logger.addHandler(handler);
            logger.setUseParentHandlers(false);
        }
        
        final Logger before = loggerCache.putIfAbsent(name, logger);
        if (before != null) {
            return before;
        }
        return logger;
    }

    public void shutdown() {
        loggerCache = null;
    }

}
