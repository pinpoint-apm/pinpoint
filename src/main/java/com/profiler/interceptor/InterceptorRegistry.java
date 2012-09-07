package com.profiler.interceptor;

import java.util.concurrent.atomic.AtomicInteger;

public class InterceptorRegistry {

    private static final Interceptor DUMMY = new LoggingInterceptor("com.profiler.interceptor.DUMMY");
    public static final InterceptorRegistry REGISTRY = new InterceptorRegistry();

	private final static int DEFAULT_MAX = 1024;
	private final int max;

	private final AtomicInteger id = new AtomicInteger(0);
	private final Interceptor[] index;

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
        Interceptor interceptor = index[key];
        if (interceptor == null) {
            // 로직이 잘못되었을 경우 에러가 발생하지 않도록 더미를 리턴.
            return DUMMY;
        }
        return interceptor;
	}

	public static int addInterceptor(Interceptor interceptor) {
		return REGISTRY.addInterceptor0(interceptor);
	}

	public static Interceptor getInterceptor(int key) {
		return REGISTRY.getInterceptor0(key);
	}

}
