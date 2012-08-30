package com.profiler.interceptor;

import java.util.concurrent.atomic.AtomicInteger;

public class InterceptorRegistry {

	private final static int DEFAULT_MAX = 1024;
	private final int max;

	private final AtomicInteger id = new AtomicInteger(0);
	private final Interceptor[] index;
	// private final ConcurrentMap<String, List<Integer>> nameToIndex = new
	// ConcurrentHashMap<String, List<Integer>>();

	public static final InterceptorRegistry REGISTRY = new InterceptorRegistry();

	InterceptorRegistry() {
		this(DEFAULT_MAX);
	}

	InterceptorRegistry(int max) {
		this.max = max;
		this.index = new Interceptor[max];
	}

	int addInterceptor0(Interceptor interceptor) {
		if (interceptor == null) {
			return -1;
		}
		int newId = id.getAndIncrement();
		if (newId > max) {
			throw new IllegalArgumentException("id" + id);
		}

		this.index[newId] = interceptor;
		return newId;
	}

	Interceptor getInterceptor0(int key) {
		return index[key];
	}

	public static int addInterceptor(Interceptor interceptor) {
		return REGISTRY.addInterceptor0(interceptor);
	}

	public static Interceptor getInterceptor(int key) {
		return REGISTRY.getInterceptor0(key);
	}
}
