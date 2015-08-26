package com.navercorp.pinpoint.bootstrap.interceptor.tracevalue;

/**
 * @author emeroad
 */
public class ObjectTraceValue4Utils {

    public static Object __getTraceObject4(Object target, Object defaultValue) {
        if (target == null) {
            return defaultValue;
        }
        if (target instanceof ObjectTraceValue4) {
            final Object result = ((ObjectTraceValue4) target).__getTraceObject4();
            if (result == null) {
                return defaultValue;
            }
            return result;
        }
        return defaultValue;
    }

    public static void __setTraceObject4(Object target, Object value) {
        if (target == null) {
            return;
        }
        if (target instanceof ObjectTraceValue4) {
            ((ObjectTraceValue4) target).__setTraceObject4(value);
        }
    }
}
