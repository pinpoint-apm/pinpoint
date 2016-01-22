package com.navercorp.pinpoint.bootstrap.logging;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Handler;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class BootLoggerBinder implements PLoggerBinder {
    
    public final static Handler[] handlers = LogManager.getLogManager().getLogger("").getHandlers();

    private ConcurrentMap<String, PLogger> loggerCache = new ConcurrentHashMap<String, PLogger>(256, 0.75f, 128);

    @Override
    public PLogger getLogger(String name) {

        final PLogger hitPLogger = loggerCache.get(name);
        if (hitPLogger != null) {
            return hitPLogger;
        }

        Logger javaLogger = Logger.getLogger(name);
        
        for (Handler handler : handlers) {
            javaLogger.addHandler(handler);
            javaLogger.setUseParentHandlers(false);
        }
        
        final JavaPLoggerAdapter javaLoggerAdapter = new JavaPLoggerAdapter(javaLogger);
        final PLogger before = loggerCache.putIfAbsent(name, javaLoggerAdapter);
        if (before != null) {
            return before;
        }
        return javaLoggerAdapter;
    }

    @Override
    public void shutdown() {
        // Maybe we don't need to do this. Unregistering LoggerFactory would be enough.
        loggerCache = null;
    }

}
