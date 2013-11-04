package com.nhn.pinpoint.profiler.logging;

/**
 * @author emeroad
 */
public class DummyPLogger implements PLogger {

    public static final PLogger INSTANCE = new DummyPLogger();

    @Override
    public void beforeInterceptor(Object target, String className, String methodName, String parameterDescription, Object[] args) {
    }

    @Override
    public void beforeInterceptor(Object target, Object[] args) {
    }


    @Override
    public void afterInterceptor(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {

    }

    @Override
    public void afterInterceptor(Object target, Object[] args, Object result) {
    }


    @Override
    public void afterInterceptor(Object target, String className, String methodName, String parameterDescription, Object[] args) {
    }

    @Override
    public void afterInterceptor(Object target, Object[] args) {
    }

    @Override
    public boolean isTraceEnabled() {
        return false;
    }

    @Override
    public void trace(String msg) {

    }

    @Override
    public void trace(String format, Object arg) {

    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {

    }

    @Override
    public void trace(String format, Object[] argArray) {

    }

    @Override
    public void trace(String msg, Throwable t) {

    }

    @Override
    public boolean isDebugEnabled() {
        return false;
    }

    @Override
    public void debug(String msg) {

    }

    @Override
    public void debug(String format, Object arg) {

    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {

    }

    @Override
    public void debug(String format, Object[] argArray) {

    }

    @Override
    public void debug(String msg, Throwable t) {

    }

    @Override
    public boolean isInfoEnabled() {
        return false;
    }

    @Override
    public void info(String msg) {

    }

    @Override
    public void info(String format, Object arg) {

    }

    @Override
    public void info(String format, Object arg1, Object arg2) {

    }

    @Override
    public void info(String format, Object[] argArray) {

    }

    @Override
    public void info(String msg, Throwable t) {

    }

    @Override
    public boolean isWarnEnabled() {
        return false;
    }

    @Override
    public void warn(String msg) {

    }

    @Override
    public void warn(String format, Object arg) {

    }

    @Override
    public void warn(String format, Object[] argArray) {

    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {

    }

    @Override
    public void warn(String msg, Throwable t) {

    }

    @Override
    public boolean isErrorEnabled() {
        return false;
    }

    @Override
    public void error(String msg) {

    }

    @Override
    public void error(String format, Object arg) {

    }

    @Override
    public void error(String format, Object arg1, Object arg2) {

    }

    @Override
    public void error(String format, Object[] argArray) {

    }

    @Override
    public void error(String msg, Throwable t) {

    }
}
