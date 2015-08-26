package com.navercorp.pinpoint.bootstrap.interceptor.tracevalue;

/**
 * @author emeroad
 */
public class ObjectTraceValue3Utils {

    public static Object __getTraceObject3(Object target, Object defaultValue) {
        if (target == null) {
            return defaultValue;
        }
        if (target instanceof ObjectTraceValue3) {
            final Object result = ((ObjectTraceValue3) target).__getTraceObject3();
            if (result == null) {
                return defaultValue;
            }
            return result;
        }
        return defaultValue;
    }

    public static void __setTraceObject3(Object target, Object value) {
        if (target == null) {
            return;
        }
        if (target instanceof ObjectTraceValue3) {
            ((ObjectTraceValue3) target).__setTraceObject3(value);
        }
    }
}
