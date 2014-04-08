package com.nhn.pinpoint.profiler.logging;

import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerBinder;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author emeroad
 */
public class Slf4jLoggerBinder implements PLoggerBinder {

    private ConcurrentMap<String, PLogger> loggerCache = new ConcurrentHashMap<String, PLogger>(256, 0.75f, 32);

    @Override
    public PLogger getLogger(String name) {

        final PLogger hitPLogger = loggerCache.get(name);
        if (hitPLogger != null) {
            return hitPLogger;
        }

        org.slf4j.Logger slf4jLogger = LoggerFactory.getLogger(name);

        final Slf4jPLoggerAdapter slf4jLoggerAdapter = new Slf4jPLoggerAdapter(slf4jLogger);
        final PLogger before = loggerCache.putIfAbsent(name, slf4jLoggerAdapter);
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
