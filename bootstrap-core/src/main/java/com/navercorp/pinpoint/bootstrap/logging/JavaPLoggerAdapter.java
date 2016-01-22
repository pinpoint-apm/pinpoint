package com.navercorp.pinpoint.bootstrap.logging;

import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaPLoggerAdapter implements PLogger {

    private final Logger logger;
    public JavaPLoggerAdapter(Logger logger) {
        if (logger == null) {
            throw new NullPointerException("logger must not be null");
        }
        this.logger = logger;
    }

    public String getName() {
        return logger.getName();
    }
    
    @Override
    public void beforeInterceptor(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public void beforeInterceptor(Object target, Object[] args) {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public void afterInterceptor(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result, Throwable throwable) {
        throw new UnsupportedOperationException();
        
    }

    @Override
    public void afterInterceptor(Object target, Object[] args, Object result, Throwable throwable) {
        throw new UnsupportedOperationException(); 
    }

    @Override
    public void afterInterceptor(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void afterInterceptor(Object target, Object[] args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isLoggable(Level.FINEST);
    }

    @Override
    public void trace(String msg) {
        logger.finest(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        logger.finest(format);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        logger.finest(format);
    }

    @Override
    public void trace(String format, Object[] argArray) {
        logger.finest(format);
    }

    @Override
    public void trace(String msg, Throwable t) {
        logger.finest(msg);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isLoggable(Level.CONFIG);
    }

    @Override
    public void debug(String msg) {
        logger.config(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        logger.config(format);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        logger.config(format);
    }

    @Override
    public void debug(String format, Object[] argArray) {
        logger.config(format);
    }

    @Override
    public void debug(String msg, Throwable t) {
        logger.config(msg);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isLoggable(Level.INFO);
    }

    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        logger.info(format);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        logger.info(format);
    }

    @Override
    public void info(String format, Object[] argArray) {
        logger.info(format);
    }

    @Override
    public void info(String msg, Throwable t) {
        logger.info(msg);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isLoggable(Level.WARNING);
    }

    @Override
    public void warn(String msg) {
        logger.warning(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        logger.warning(format);
    }

    @Override
    public void warn(String format, Object[] argArray) {
        logger.warning(format);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        logger.warning(format);
    }

    @Override
    public void warn(String msg, Throwable t) {
        logger.warning(msg);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isLoggable(Level.SEVERE);
    }

    @Override
    public void error(String msg) {
        logger.severe(msg);
    }

    @Override
    public void error(String format, Object arg) {
        logger.severe(format);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        logger.severe(format);
    }

    @Override
    public void error(String format, Object[] argArray) {
        logger.severe(format);
    }

    @Override
    public void error(String msg, Throwable t) {
        logger.severe(msg);
    }
}