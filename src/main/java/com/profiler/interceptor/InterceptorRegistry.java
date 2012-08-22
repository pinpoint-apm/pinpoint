package com.profiler.interceptor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InterceptorRegistry {

    private static final ConcurrentMap<Integer, Interceptor> INTERCEPTOR_MAP = new ConcurrentHashMap<Integer, Interceptor>(256);

    public static void addInterceptor(Integer key, Interceptor interceptor) {
        INTERCEPTOR_MAP.put(key, interceptor);
    }

    public static Interceptor getInterceptor(int key) {
        return INTERCEPTOR_MAP.get(key);
    }

}
