package com.navercorp.pinpoint.bootstrap.interceptor.tracevalue;

/**
 * @author emeroad
 */
public class ObjectTraceValue1Utils {

    public static Object __getTraceObject1(Object target, Object defaultValue) {
        if (target == null) {
            return defaultValue;
        }
        if (target instanceof ObjectTraceValue1) {
            final Object result = ((ObjectTraceValue1) target).__getTraceObject1();
            if (result == null) {
                return defaultValue;
            }
            return result;
        }
        return defaultValue;
    }

    public static void __setTraceObject1(Object target, Object value) {
        if (target == null) {
            return;
        }
        if (target instanceof ObjectTraceValue1) {
            ((ObjectTraceValue1) target).__setTraceObject1(value);
        }
    }
}
