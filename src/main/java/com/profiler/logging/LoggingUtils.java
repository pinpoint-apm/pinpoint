package com.profiler.logging;

import com.profiler.util.StringUtils;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class LoggingUtils {

    public static boolean isDebug(Logger logger) {
        return logger.isLoggable(Level.FINE);
    }

    public static void logBefore(Logger logger, Object target, String className, String methodName, String parameterDescription, Object[] args) {
        StringBuilder sb = new StringBuilder(512);
        sb.append("before ");
        logMethod(sb, target, className, methodName, parameterDescription, args);
        logger.fine(sb.toString());
//        logger.fine("before " + StringUtils.toString(target) + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
    }

    public static void logAfter(Logger logger, Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        StringBuilder sb = new StringBuilder(512);
        sb.append("after ");
        logMethod(sb, target, className, methodName, parameterDescription, args);
        sb.append(" result:");
        sb.append(result);
        logger.fine(sb.toString());
    }

    public static void logAfter(Logger logger, Object target, String className, String methodName, String parameterDescription, Object[] args) {
        StringBuilder sb = new StringBuilder(512);
        sb.append("after ");
        logMethod(sb, target, className, methodName, parameterDescription, args);
        logger.fine(sb.toString());
    }

    private static void logMethod(StringBuilder sb, Object target, String className, String methodName, String parameterDescription, Object[] args) {
        sb.append(StringUtils.toString(target));
        sb.append(' ');
        sb.append(className);
        sb.append(' ');
        sb.append(methodName);
        sb.append(parameterDescription);
        sb.append(" args:");
        sb.append(Arrays.toString(args));
    }
}
