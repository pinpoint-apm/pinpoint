package com.profiler.interceptor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InterceptorRegistry {

    private static final ConcurrentMap<String, Interceptor> INTERCEPTO_MAP = new ConcurrentHashMap<String, Interceptor>(256);

    public static void addInterceptor(String className, Interceptor interceptor) {
        INTERCEPTO_MAP.put(className, interceptor);
    }

    public static Interceptor getInterceptor(String className) {
        return INTERCEPTO_MAP.get(className);
    }

}
