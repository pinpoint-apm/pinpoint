package com.navercorp.pinpoint.profiler.logging;


import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerBinder;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.Assert;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Log4jLoggingSystem implements LoggingSystem {

    private final String configPath;
    private Logger logger;

    private PLoggerBinder binder;

    public Log4jLoggingSystem(String configPath) {
        this.configPath = Assert.requireNonNull(configPath, "configPath");
    }

    @Override
    public void start() {
        initializeLogger(this.configPath);
        this.logger = LoggerFactory.getLogger(this.getClass());
        logger.info("Log4jLoggingSystem start");

        this.binder = new Slf4jLoggerBinder();
        bindPLoggerFactory(this.binder);
    }

    @Override
    public void stop() {
        if (logger != null) {
            logger.debug("Log4jLoggingSystem stop");
        }
        if (this.binder != null) {
            binder.shutdown();
        }
    }

    private void initializeLogger(String location) {
        // log4j init
        DOMConfigurator.configure(location);
    }

    private void bindPLoggerFactory(PLoggerBinder binder) {
        final String binderClassName = binder.getClass().getName();
        PLogger pLogger = binder.getLogger(binder.getClass().getName());
        pLogger.info("PLoggerFactory.initialize() bind:{} cl:{}", binderClassName, binder.getClass().getClassLoader());
        // Set binder to static LoggerFactory
        // Should we unset binder at shutdown hook or stop()?
        PLoggerFactory.initialize(binder);
    }
}
