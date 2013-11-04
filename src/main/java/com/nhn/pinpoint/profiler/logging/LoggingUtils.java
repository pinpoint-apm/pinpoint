package com.nhn.pinpoint.profiler.logging;

import java.util.Arrays;

/**
 * @author emeroad
 */
public class LoggingUtils {


    public static void logBefore(PLogger logger, Object target, String className, String methodName, String parameterDescription, Object[] args) {
        StringBuilder sb = new StringBuilder(512);
        sb.append("before ");
        logMethod(sb, getTarget(target), className, methodName, parameterDescription, args);
        logger.debug(sb.toString());
    }

    public static void logAfter(PLogger logger, Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {
        StringBuilder sb = new StringBuilder(512);
        sb.append("after ");
        logMethod(sb, getTarget(target), className, methodName, parameterDescription, args);
        sb.append(" result:");
        sb.append(getTarget(result));
        logger.debug(sb.toString());
    }

    public static void logAfter(PLogger logger, Object target, String className, String methodName, String parameterDescription, Object[] args) {
        StringBuilder sb = new StringBuilder(512);
        sb.append("after ");
        logMethod(sb, getTarget(target), className, methodName, parameterDescription, args);
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
        sb.append(Arrays.toString(args));
    }

    private static Object getTarget(Object target) {
        if (target == null) {
            return "target=null";
        }
        return target.getClass().getName();
    }


}
