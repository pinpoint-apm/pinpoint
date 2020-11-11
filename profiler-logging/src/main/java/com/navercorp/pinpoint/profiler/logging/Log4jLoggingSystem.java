package com.navercorp.pinpoint.profiler.logging;


import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerBinder;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.util.Assert;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.logging.Handler;

public class Log4jLoggingSystem implements LoggingSystem {

    private final String profilePath;
    private Logger logger;

    private PLoggerBinder binder;

    public Log4jLoggingSystem(String profilePath) {
        this.profilePath = Assert.requireNonNull(profilePath, "profilePath");
    }

    @Override
    public void start() {
        String configPath = String.format("%s/%s", profilePath, "log4j.xml");
        initializeLogger(configPath);
        this.logger = LoggerFactory.getLogger(this.getClass());
        logger.info("{} start {}", this.getClass().getSimpleName(), configPath);

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
        LoggerRepository loggerRepository = LogManager.getLoggerRepository();

        DOMConfigurator domConfigurator = new DOMConfigurator();
        domConfigurator.doConfigure(location,loggerRepository);
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
