package com.navercorp.pinpoint.profiler.cache;

import com.github.benmanes.caffeine.cache.Caffeine;

public class CaffeineBuilder {

    static final ExecutorManager MANAGER = new ExecutorManager();

    public static final int MAX_CPU = 4;


    public static Caffeine<Object, Object> newBuilder() {
        final Caffeine<Object, Object> cacheBuilder = Caffeine.newBuilder();
        cacheBuilder.executor(MANAGER.executor());
        return cacheBuilder;
    }
}
