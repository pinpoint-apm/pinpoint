package com.nhn.pinpoint.metadata;

import com.nhn.pinpoint.common.util.BytesUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public class StringCache {
    // 0인값은 존재 하지 않음을 나타냄.
    private final AtomicInteger idGen = new AtomicInteger(1);
    private final ConcurrentMap<String, Integer> cache = new ConcurrentHashMap<String, Integer>();

    public Result put(String string) {
        Integer find = this.cache.get(string);
        if(find != null) {
            return new Result(false, find);
        }
        //음수까지 활용하여 가능한 데이터 인코딩을 작게 유지되게 함.
        int newId = BytesUtils.decodeZigZagInt(idGen.getAndIncrement());
        Integer before = this.cache.putIfAbsent(string, newId);
        if (before != null) {
            return new Result(false, before);
        }
        return new Result(true, newId);
    }

}
