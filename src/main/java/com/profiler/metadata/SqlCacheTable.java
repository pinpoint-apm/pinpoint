package com.profiler.metadata;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * approximate concurrent lru cache
 */
public class SqlCacheTable<T> {

    private static final Object V = new Object();

    private int concurrentLevel = 16;
    private Map[] entry;

    private AtomicInteger cacheSize = new AtomicInteger();
    private int maxCacheSize = 500;


    public SqlCacheTable(int maxCacheSize) {
        this.maxCacheSize = maxCacheSize;
        initialize();
    }

    public SqlCacheTable() {
        initialize();
    }

    private void initialize() {
        this.entry = new Map[concurrentLevel];
        for (int i = 0; i < concurrentLevel; i++) {
            this.entry[i] = createLinkedHashMap();
        }
    }

    private Map createLinkedHashMap() {

        LinkedHashMap map = new LinkedHashMap(200, .75F, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry eldest) {
                // 해당 cache는 구현은 매우 정확하게 max사이를 가지고 있지 않음, 어느정도 오차가 있는 범위에서 동작한다..
                boolean remove = cacheSize.get() + 1 > maxCacheSize;
                // + 1의 경우 성능을 좀더 높이기 위해서 put이후 정상적으로 들어갔을 경우 count를 increment시키기 때문에 먼저 +1해서 봄
                // +-대략 concurrentLevel 정도의 오차가 생길수 있을것으로 추정함.
                if (remove) {
                    cacheSize.getAndDecrement();
                }
                return remove;
            }
        };
        return Collections.synchronizedMap(map);
    }


    public boolean put(T value) {
        Map cacheMap = getHashEntry(value);
        Object oldValue = cacheMap.put(value, V);
        if (oldValue == null) {
            cacheSize.incrementAndGet();
            return true;
        }
        return false;
    }

    private Map getHashEntry(T key) {

        int entryNumber = Math.abs(key.hashCode()) % concurrentLevel;

        return this.entry[entryNumber];
    }

    public int getSize() {
        return cacheSize.get();
    }

}
