package com.nhn.pinpoint.logging;

import com.nhn.pinpoint.profiler.logging.Logger;
import com.nhn.pinpoint.profiler.logging.LoggerBinder;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 */
public class Slf4jLoggerBinder implements LoggerBinder {

    private ConcurrentMap<String, Logger> loggerCache = new ConcurrentHashMap<String, Logger>();

    @Override
    public Logger getLogger(String name) {

        Logger hitLogger = loggerCache.get(name);
        if (hitLogger != null) {
            return hitLogger;
        }

        org.slf4j.Logger slf4jLogger = LoggerFactory.getLogger(name);

        Slf4jLoggerAdapter slf4jLoggerAdapter = new Slf4jLoggerAdapter(slf4jLogger);
        Logger before = loggerCache.putIfAbsent(name, slf4jLoggerAdapter);
        if (before != null) {
            return before;
        }
        return slf4jLoggerAdapter;
    }

    @Override
    public void shutdown() {
        // 안해도 될것도 같고. LoggerFactory의unregister만  해도 될려나?
        loggerCache = null;
    }
}
