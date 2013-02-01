package com.profiler.metadata;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class StringCache {

    private final AtomicInteger idGen = new AtomicInteger();
    private final ConcurrentMap<String, Integer> cache = new ConcurrentHashMap<String, Integer>();

    public Result put(String string) {
        Integer find = this.cache.get(string);
        if(find != null) {
            return new Result(false, find);
        }

        int newId = idGen.getAndIncrement();
        Integer before = this.cache.putIfAbsent(string, newId);
        if (before != null) {
            return new Result(false, before);
        }
        return new Result(true, newId);
    }

}
