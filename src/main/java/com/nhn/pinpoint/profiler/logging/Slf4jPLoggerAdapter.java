package com.nhn.pinpoint.profiler.logging;

import org.slf4j.Marker;

/**
 *
 */
public class Slf4jPLoggerAdapter implements PLogger {
    private final org.slf4j.Logger logger;

    public static final int BUFFER_SIZE = 512;

    public Slf4jPLoggerAdapter(org.slf4j.Logger logger) {
        this.logger = logger;
    }

    public String getName() {
        return logger.getName();
    }

    @Override
    public void beforeInterceptor(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        StringBuilder sb = new StringBuilder(BUFFER_SIZE);
        sb.append("before ");
        logMethod(sb, target, className, methodName, parameterDescription, args);
        logger.debug(sb.toString());
    }

    @Override
    public void beforeInterceptor(Object target, Object[] args) {
        StringBuilder sb = new StringBuilder(BUFFER_SIZE);
        sb.append("before ");
        logMethod(sb, target, args);
        logger.debug(sb.toString());
    }

    @Override
    public void afterInterceptor(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        StringBuilder sb = new StringBuilder(BUFFER_SIZE);
        sb.append("after ");
        logMethod(sb, target, className, methodName, parameterDescription, args);
        sb.append(" result:");
        sb.append(result);
        logger.debug(sb.toString());
    }

    @Override
    public void afterInterceptor(Object target, Object[] args, Object result) {
        StringBuilder sb = new StringBuilder(BUFFER_SIZE);
        sb.append("after ");
        logMethod(sb, target, args);
        sb.append(" result:");
        sb.append(result);
        logger.debug(sb.toString());
    }

    @Override
    public void afterInterceptor(Object target, String className, String methodName, String parameterDescription, Object[] args) {
        StringBuilder sb = new StringBuilder(BUFFER_SIZE);
        sb.append("after ");
        logMethod(sb, target, className, methodName, parameterDescription, args);
        logger.debug(sb.toString());
    }

    @Override
    public void afterInterceptor(Object target, Object[] args) {
        StringBuilder sb = new StringBuilder(BUFFER_SIZE);
        sb.append("after ");
        logMethod(sb, target, args);
        logger.debug(sb.toString());
    }

    private static void logMethod(StringBuilder sb, Object target, String className, String methodName, String parameterDescription, Object[] args) {
        sb.append(getTarget(target));
        sb.append(' ');
        sb.append(className);
        sb.append(' ');
        sb.append(methodName);
        sb.append(parameterDescription);
        sb.append(" args:");
        appendArray(sb, args);
    }

    private static void logMethod(StringBuilder sb, Object target, Object[] args) {
        sb.append(getTarget(target));
        sb.append(' ');
        sb.append(" args:");
        appendArray(sb, args);
    }

    private static String getTarget(Object target) {
        // toString의 경우 sideeffect가 발생할수 있으므로 className을 호출하는것으로 변경함.
        if (target == null) {
            return "target=null";
        } else {
            return target.getClass().getName();
        }
    }

    private static void appendArray(StringBuilder sb, Object[] args) {
        if (args == null) {
            sb.append("null");
            return;
        }
        if (args.length == 0) {
            return;
        }

        if (args.length > 0) {
            sb.append('(');
            sb.append(args[0]);
            for (int i = 1; i < args.length; i++) {
                sb.append(", ");
                sb.append(args[i]);
            }
            sb.append(')');
        }
        return;
    }

    @Override
    public boolean isTraceEnabled() {
        return logger.isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        logger.trace(msg);
    }

    @Override
    public void trace(String format, Object arg) {
        logger.trace(format, arg);
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        logger.trace(format, arg1, arg2);
    }

    @Override
    public void trace(String format, Object[] argArray) {
        logger.trace(format, argArray);
    }

    @Override
    public void trace(String msg, Throwable t) {
        logger.trace(msg, t);
    }

    public boolean isTraceEnabled(Marker marker) {
        return logger.isTraceEnabled(marker);
    }

    public void trace(Marker marker, String msg) {
        logger.trace(marker, msg);
    }

    public void trace(Marker marker, String format, Object arg) {
        logger.trace(marker, format, arg);
    }

    public void trace(Marker marker, String format, Object arg1, Object arg2) {
        logger.trace(marker, format, arg1, arg2);
    }

    public void trace(Marker marker, String format, Object[] argArray) {
        logger.trace(marker, format, argArray);
    }

    public void trace(Marker marker, String msg, Throwable t) {
        logger.trace(marker, msg, t);
    }

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        logger.debug(msg);
    }

    @Override
    public void debug(String format, Object arg) {
        logger.debug(format, arg);
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        logger.debug(format, arg1, arg2);
    }

    @Override
    public void debug(String format, Object[] argArray) {
        logger.debug(format, argArray);
    }

    @Override
    public void debug(String msg, Throwable t) {
        logger.debug(msg, t);
    }

    public boolean isDebugEnabled(Marker marker) {
        return logger.isDebugEnabled(marker);
    }

    public void debug(Marker marker, String msg) {
        logger.debug(marker, msg);
    }

    public void debug(Marker marker, String format, Object arg) {
        logger.debug(marker, format, arg);
    }

    public void debug(Marker marker, String format, Object arg1, Object arg2) {
        logger.debug(marker, format, arg1, arg2);
    }

    public void debug(Marker marker, String format, Object[] argArray) {
        logger.debug(marker, format, argArray);
    }

    public void debug(Marker marker, String msg, Throwable t) {
        logger.debug(marker, msg, t);
    }

    @Override
    public boolean isInfoEnabled() {
        return logger.isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        logger.info(msg);
    }

    @Override
    public void info(String format, Object arg) {
        logger.info(format, arg);
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        logger.info(format, arg1, arg2);
    }

    @Override
    public void info(String format, Object[] argArray) {
        logger.info(format, argArray);
    }

    @Override
    public void info(String msg, Throwable t) {
        logger.info(msg, t);
    }

    public boolean isInfoEnabled(Marker marker) {
        return logger.isInfoEnabled(marker);
    }

    public void info(Marker marker, String msg) {
        logger.info(marker, msg);
    }

    public void info(Marker marker, String format, Object arg) {
        logger.info(marker, format, arg);
    }

    public void info(Marker marker, String format, Object arg1, Object arg2) {
        logger.info(marker, format, arg1, arg2);
    }

    public void info(Marker marker, String format, Object[] argArray) {
        logger.info(marker, format, argArray);
    }

    public void info(Marker marker, String msg, Throwable t) {
        logger.info(marker, msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return logger.isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        logger.warn(msg);
    }

    @Override
    public void warn(String format, Object arg) {
        logger.warn(format, arg);
    }

    @Override
    public void warn(String format, Object[] argArray) {
        logger.warn(format, argArray);
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        logger.warn(format, arg1, arg2);
    }

    @Override
    public void warn(String msg, Throwable t) {
        logger.warn(msg, t);
    }

    public boolean isWarnEnabled(Marker marker) {
        return logger.isWarnEnabled(marker);
    }

    public void warn(Marker marker, String msg) {
        logger.warn(marker, msg);
    }

    public void warn(Marker marker, String format, Object arg) {
        logger.warn(marker, format, arg);
    }

    public void warn(Marker marker, String format, Object arg1, Object arg2) {
        logger.warn(marker, format, arg1, arg2);
    }

    public void warn(Marker marker, String format, Object[] argArray) {
        logger.warn(marker, format, argArray);
    }

    public void warn(Marker marker, String msg, Throwable t) {
        logger.warn(marker, msg, t);
    }

    @Override
    public boolean isErrorEnabled() {
        return logger.isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        logger.error(msg);
    }

    @Override
    public void error(String format, Object arg) {
        logger.error(format, arg);
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        logger.error(format, arg1, arg2);
    }

    @Override
    public void error(String format, Object[] argArray) {
        logger.error(format, argArray);
    }

    @Override
    public void error(String msg, Throwable t) {
        logger.error(msg, t);
    }

    public boolean isErrorEnabled(Marker marker) {
        return logger.isErrorEnabled(marker);
    }

    public void error(Marker marker, String msg) {
        logger.error(marker, msg);
    }

    public void error(Marker marker, String format, Object arg) {
        logger.error(marker, format, arg);
    }

    public void error(Marker marker, String format, Object arg1, Object arg2) {
        logger.error(marker, format, arg1, arg2);
    }

    public void error(Marker marker, String format, Object[] argArray) {
        logger.error(marker, format, argArray);
    }

    public void error(Marker marker, String msg, Throwable t) {
        logger.error(marker, msg, t);
    }
}
