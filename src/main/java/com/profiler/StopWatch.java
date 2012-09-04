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
			throw new IllegalStateException("Stopwatch is not started.");
		} else {
			return System.nanoTime() - map.get(name);
		}
	}
}
