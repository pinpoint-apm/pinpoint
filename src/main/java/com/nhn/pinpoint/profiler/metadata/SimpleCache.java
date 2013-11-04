package com.nhn.pinpoint.profiler.metadata;

import com.nhn.pinpoint.common.util.BytesUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author emeroad
 */
public class SimpleCache<T> {
    // 0인값은 존재 하지 않음을 나타냄.
    private final AtomicInteger idGen;
    private final ConcurrentMap<T, Result> cache = new ConcurrentHashMap<T, Result>(512, 0.75f, 32);

    public SimpleCache() {
        this(1);
    }

    public SimpleCache(int startValue) {
        idGen = new AtomicInteger(startValue);
    }

    public Result put(T value) {
        final Result find = this.cache.get(value);
        if (find != null) {
            return find;
        }
        //음수까지 활용하여 가능한 데이터 인코딩을 작게 유지되게 함.
        final int newId = BytesUtils.zigzagToInt(idGen.getAndIncrement());
        final Result result = new Result(false, newId);
        final Result before = this.cache.putIfAbsent(value, result);
        if (before != null) {
            return before;
        }
        return new Result(true, newId);
    }

}
