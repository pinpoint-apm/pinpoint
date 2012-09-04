package com.profiler;

import java.util.HashMap;
import java.util.Map;

import com.profiler.util.NamedThreadLocal;

public class StopWatch {

	private static ThreadLocal<Map<String, Long>> local = new NamedThreadLocal<Map<String, Long>>("StopWatch");

	public static void start(String name) {
		Map<String, Long> map = local.get();
		if (map == null) {
			map = new HashMap<String, Long>(1);
			map.put(name, System.nanoTime());
			local.set(map);
		} else {
			map.put(name, System.nanoTime());
		}
	}

	public static long stopAndGetElapsed(String name) {
		Map<String, Long> map = local.get();
		if (map == null) {
			//throw new IllegalStateException("Stopwatch is not started.");
            // TODO application 에러로 전달되는경우가 있어서 일단 0으로
            return 0;
		} else {
			return System.nanoTime() - map.get(name);
		}
	}
}
