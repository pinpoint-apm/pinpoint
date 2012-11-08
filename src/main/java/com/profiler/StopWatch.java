package com.profiler;

import java.util.HashMap;
import java.util.Map;

import com.profiler.util.NamedThreadLocal;

@Deprecated
public class StopWatch {

    private static ThreadLocal<Map<String, Long>> local = new NamedThreadLocal<Map<String, Long>>("StopWatch");

    public static void start(int id) {
        start(String.valueOf(id));
    }

    public static long stopAndGetElapsed(int id) {
        return stopAndGetElapsed(String.valueOf(id));
    }

    public static void start(String id) {
        Map<String, Long> map = local.get();
        if (map == null) {
            map = new HashMap<String, Long>(1);
            map.put(id, System.nanoTime());
            local.set(map);
        } else {
            map.put(id, System.nanoTime());
        }
    }

    public static long stopAndGetElapsed(String id) {
        Map<String, Long> map = local.get();
        if (map == null) {
            // throw new IllegalStateException("Stopwatch is not started.");
            // TODO application 에러로 전달되는경우가 있어서 일단 0으로
            return -1;
        } else {
            Long startTime = map.get(id);

            map.remove(id);
            // TODO: if using thread pool, this is unnecessary.
            // if (map.size() == 0) {
            // local.remove();
            // }

            if (startTime != null) {
                return System.nanoTime() - startTime;
            } else {
                return -1;
            }
        }
    }
}
