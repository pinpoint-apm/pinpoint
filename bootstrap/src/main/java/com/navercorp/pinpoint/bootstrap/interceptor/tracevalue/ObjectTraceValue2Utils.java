package com.navercorp.pinpoint.bootstrap.interceptor.tracevalue;

/**
 * @author emeroad
 */
public class ObjectTraceValue2Utils {

    public static Object __getTraceObject2(Object target, Object defaultValue) {
        if (target == null) {
            return defaultValue;
        }
        if (target instanceof ObjectTraceValue2) {
            final Object result = ((ObjectTraceValue2) target).__getTraceObject2();
            if (result == null) {
                return defaultValue;
            }
            return result;
        }
        return defaultValue;
    }

    public static void __setTraceObject2(Object target, Object value) {
        if (target == null) {
            return;
        }
        if (target instanceof ObjectTraceValue2) {
            ((ObjectTraceValue2) target).__setTraceObject2(value);
        }
    }

    public static void __setTraceObject2(Object target, Object value, Class expectedClassType) {
        if (target == null) {
            return;
        }

        if (target instanceof ObjectTraceValue2) {
            ((ObjectTraceValue2) target).__setTraceObject2(value);
        }
    }
}
