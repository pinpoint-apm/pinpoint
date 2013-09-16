package com.nhn.pinpoint.profiler.logging;

import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 *
 */
public class Slf4jLoggerBinder implements PLoggerBinder {

    private ConcurrentMap<String, PLogger> loggerCache = new ConcurrentHashMap<String, PLogger>();

    @Override
    public PLogger getLogger(String name) {

        PLogger hitPLogger = loggerCache.get(name);
        if (hitPLogger != null) {
            return hitPLogger;
        }

        org.slf4j.Logger slf4jLogger = LoggerFactory.getLogger(name);

        Slf4jPLoggerAdapter slf4jLoggerAdapter = new Slf4jPLoggerAdapter(slf4jLogger);
        PLogger before = loggerCache.putIfAbsent(name, slf4jLoggerAdapter);
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
