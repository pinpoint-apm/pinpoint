package com.profiler.util;

public class InterceptorUtils {
    public static boolean isThrowable(Object result) {
        if (result instanceof Throwable) {
            return true;
        }
        return false;
    }

    public static boolean isSuccess(Object result) {
        return !isThrowable(result);
    }
}
