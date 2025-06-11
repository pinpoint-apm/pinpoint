package com.navercorp.pinpoint.profiler.logging.jul;

import org.apache.logging.log4j.LogManager;

public class Logger {
    private org.apache.logging.log4j.Logger logger;

    public static Logger getAnonymousLogger() {
        return new Logger(LogManager.getRootLogger());
    }

    public static Logger getLogger(String name) {
        org.apache.logging.log4j.Logger logger = LogManager.getLogger(name);
        return new Logger(logger);
    }

    Logger(org.apache.logging.log4j.Logger logger) {
        this.logger = logger;
    }

    public void log(Level level, String msg) {
        this.logger.log(JulLogLevel.toLevel(level.intValue()), msg);
    }

    public void log(Level level, String msg, Object param1) {
        this.logger.log(JulLogLevel.toLevel(level.intValue()), msg, param1);
    }

    public void log(Level level, String msg, Object params[]) {
        this.logger.log(JulLogLevel.toLevel(level.intValue()), msg, params);
    }

    public void log(Level level, String msg, Throwable thrown) {
        this.logger.log(JulLogLevel.toLevel(level.intValue()), msg, thrown);
    }

    public void severe(String msg) {
        this.logger.fatal(msg);
    }

    public void warning(String msg) {
        this.logger.warn(msg);
    }

    public void info(String msg) {
        this.logger.info(msg);
    }

    public void config(String msg) {
        this.logger.debug(msg);
    }

    public void fine(String msg) {
        this.logger.debug(msg);
    }

    public void finer(String msg) {
        this.logger.trace(msg);
    }

    public void finest(String msg) {
        this.logger.trace(msg);
    }
}
