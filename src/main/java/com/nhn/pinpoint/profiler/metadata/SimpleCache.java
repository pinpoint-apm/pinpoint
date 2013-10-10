package com.nhn.pinpoint.profiler.metadata;

import com.nhn.pinpoint.common.util.BytesUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class SimpleCache<T> {
    // 0인값은 존재 하지 않음을 나타냄.
    private final AtomicInteger idGen = new AtomicInteger(1);
    private final ConcurrentMap<T, Integer> cache = new ConcurrentHashMap<T, Integer>();

    public Result put(T value) {
        Integer find = this.cache.get(value);
        if(find != null) {
            return new Result(false, find);
        }
        //음수까지 활용하여 가능한 데이터 인코딩을 작게 유지되게 함.
        int newId = BytesUtils.decodeZigZagInt(idGen.getAndIncrement());
        Integer before = this.cache.putIfAbsent(value, newId);
        if (before != null) {
            return new Result(false, before);
        }
        return new Result(true, newId);
    }

}
