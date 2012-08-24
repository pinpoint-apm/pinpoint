package com.profiler.interceptor;

import java.util.concurrent.atomic.AtomicInteger;

public class InterceptorRegistry {

    private static final AtomicInteger ID = new AtomicInteger(0);

    private static int MAX = 1024;
    private static final Interceptor[] INDEX = new Interceptor[MAX];

    public static int addInterceptor(Interceptor interceptor) {
        int id = ID.getAndIncrement();
        if (id > MAX) {
            throw new IllegalArgumentException("id" + id);
        }
        INDEX[id] = interceptor;
        return id;
    }

    public static Interceptor getInterceptor(int key) {
        return INDEX[key];
    }

}
